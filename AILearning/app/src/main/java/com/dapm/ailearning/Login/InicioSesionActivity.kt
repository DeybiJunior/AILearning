package com.dapm.ailearning.Login

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dapm.ailearning.Datos.AppDatabase
import com.dapm.ailearning.Datos.Leccion
import com.dapm.ailearning.Datos.Usuario
import com.dapm.ailearning.MainActivity
import com.dapm.ailearning.R
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class InicioSesionActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var statusTextView: TextView

    private val maxAttempts = 3
    private val lockoutDuration = 3600000L // 1 hour in milliseconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_iniciosesion)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance() // Inicializa Firestore
        statusTextView = findViewById(R.id.status_text_view)
        val emailLayout = findViewById<TextInputLayout>(R.id.emailLayout)
        val passwordLayout = findViewById<TextInputLayout>(R.id.passwordLayout)
        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val registarseButton = findViewById<Button>(R.id.registarseButton)
        val continueWithoutLoginButton = findViewById<ImageButton>(R.id.continueWithoutLoginButton)
        val recuperarPasswordButton = findViewById<TextView>(R.id.recuperarPasswordButton)

        val prefs = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)
        var attemptCount = prefs.getInt("attemptCount", 0)
        val lockoutTime = prefs.getLong("lockoutTime", 0)

        // Revisar si el tiempo de bloqueo ha terminado
        if (SystemClock.elapsedRealtime() < lockoutTime) {
            disableButton(loginButton)
        } else {
            resetAttempts(prefs)
        }

        loginButton.setOnClickListener {
            handleLogin(
                emailLayout, emailEditText.text.toString().trim(),
                passwordLayout, passwordEditText.text.toString().trim(),
                prefs, loginButton
            )
        }

        registarseButton.setOnClickListener {
            val sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putBoolean("isFirstTime", false)
            editor.apply()

            val intentRegistro = Intent(this, RegistroActivity::class.java)
            startActivity(intentRegistro)
        }

        continueWithoutLoginButton.setOnClickListener {
            val sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putBoolean("isFirstTime", false) // Cambiar a false
            editor.apply()

            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        recuperarPasswordButton.setOnClickListener {
            val intent = Intent(this, RecuperarPasswordActivity::class.java)
            startActivity(intent)
        }
    }

    private fun handleLogin(
        emailLayout: TextInputLayout, email: String,
        passwordLayout: TextInputLayout, password: String,
        prefs: SharedPreferences, loginButton: Button
    ) {
        emailLayout.error = null
        passwordLayout.error = null

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.error = getString(R.string.error_email_inicio)
            return
        }

        if (password.isEmpty()) {
            passwordLayout.error = getString(R.string.error_password)
            return
        }

        val attemptCount = prefs.getInt("attemptCount", 0)
        if (attemptCount < maxAttempts) {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {

                        showToast(getString(R.string.success_login))

                        val sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
                        val editor = sharedPreferences.edit()
                        editor.putBoolean("isFirstTime", false)
                        editor.apply()

                        val currentUser = auth.currentUser
                        if (currentUser != null) {
                            firestore.collection("users").document(currentUser.uid).get()
                                .addOnSuccessListener { document ->
                                    if (document != null && document.exists()) {
                                        val nombres = document.getString("nombres") ?: "Nombre"
                                        val apellidos = document.getString("apellidos") ?: "Apellido"
                                        val edad = document.getLong("edad")?.toInt() ?: 0
                                        val nivel = document.getLong("nivel")?.toInt() ?: 0
                                        val section = document.getString("seccion") ?: "Sección"
                                        val grade = document.getString("grado") ?: "Grado"

                                        val usuario = Usuario(currentUser.uid, nombres, apellidos, edad, nivel, section, grade)
                                        guardarUsuarioEnSharedPreferences(usuario)

                                        obtenerYGuardarLecciones(currentUser.uid)

                                        val intent = Intent(this, MainActivity::class.java)
                                        startActivity(intent)
                                        finish()
                                    } else {
                                        showToast("Error: Usuario no encontrado")
                                    }
                                }
                                .addOnFailureListener { e ->
                                    showToast("Error al cargar datos: ${e.message}")
                                }
                        }
                    } else {
                        handleFailedLogin(task.exception, prefs, loginButton)
                    }
                }
        } else {
            showToast("Límite de intentos alcanzado. Intenta nuevamente en una hora.")
        }
    }

    private fun obtenerYGuardarLecciones(userId: String) {
        firestore.collection("users").document(userId).collection("lecciones")
            .get()
            .addOnSuccessListener { result ->
                val leccionesList = mutableListOf<Leccion>()
                for (document in result) {
                    val leccion = Leccion(
                        userId = userId,
                        tipo = document.getString("tipo") ?: "",
                        dificultad = document.getString("dificultad") ?: "",
                        tema = document.getString("tema") ?: "",
                        json = document.getString("json") ?: "",
                        respuestasSeleccionadas = document.getString("respuestasSeleccionadas") ?: "",
                        estado = document.getBoolean("estado") ?: false,
                        puntaje = document.getLong("puntaje")?.toInt() ?: 0,
                        startTime = document.getLong("startTime") ?: 0,
                        duration = document.getLong("duration") ?: 0,
                        completionDate = document.getLong("completionDate") ?: 0
                    )
                    leccionesList.add(leccion)
                }
                guardarLeccionesEnRoom(leccionesList)
            }
            .addOnFailureListener { e ->
                showToast("Error al cargar lecciones: ${e.message}")
            }
    }

    private fun guardarLeccionesEnRoom(lecciones: List<Leccion>) {
        val db = AppDatabase.getDatabase(applicationContext)
        val leccionDao = db.leccionDao()
        CoroutineScope(Dispatchers.IO).launch {
            leccionDao.insertAll(*lecciones.toTypedArray())
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun handleFailedLogin(exception: Exception?, prefs: SharedPreferences, loginButton: Button) {
        val attemptCount = prefs.getInt("attemptCount", 0) + 1
        prefs.edit().putInt("attemptCount", attemptCount).apply()

        val errorMessage = when {
            exception?.message?.contains("There is no user record") == true -> getString(R.string.error_no_user)
            exception?.message?.contains("The password is invalid") == true -> getString(R.string.error_wrong_password)
            else -> getString(R.string.error_login_failure, exception?.message)
        }
        showToast(errorMessage)


        // Bloquear si se alcanzaron los 3 intentos
        if (attemptCount >= maxAttempts) {
            val newLockoutTime = SystemClock.elapsedRealtime() + lockoutDuration
            prefs.edit().putLong("lockoutTime", newLockoutTime).apply()
            disableButton(loginButton)
        }
    }

    private fun disableButton(button: Button) {
        button.isEnabled = false
        Toast.makeText(this, "Has alcanzado el límite de intentos. Intenta nuevamente en una hora.", Toast.LENGTH_LONG).show()
    }

    private fun resetAttempts(prefs: SharedPreferences) {
        prefs.edit().putInt("attemptCount", 0).apply()
        prefs.edit().putLong("lockoutTime", 0).apply()
    }

// Modificar para incluir nivel
    private fun guardarUsuarioEnSharedPreferences(usuario: Usuario) {
        val sharedPreferences = getSharedPreferences("user_data", MODE_PRIVATE) // Cambia el nombre aquí
        val editor = sharedPreferences.edit()
        editor.putString("user_id", usuario.id)
        editor.putString("user_nombres", usuario.nombres)
        editor.putString("user_apellidos", usuario.apellidos)
        editor.putInt("user_edad", usuario.edad)
        editor.putInt("user_nivel", usuario.nivel)
        editor.apply()

        Log.d("SharedPrefs", "Guardando datos de usuario: " +
                "ID: ${usuario.id}, " +
                "Nombres: ${usuario.nombres}, " +
                "Apellidos: ${usuario.apellidos}, " +
                "Edad: ${usuario.edad}, " +
                "Nivel: ${usuario.nivel}")
    }
}
