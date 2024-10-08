package com.dapm.ailearning.Login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.dapm.ailearning.Datos.Usuario
import com.dapm.ailearning.MainActivity
import com.dapm.ailearning.R
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class InicioSesionActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var statusTextView: TextView

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

        loginButton.setOnClickListener {
            handleLogin(
                emailLayout, emailEditText.text.toString().trim(),
                passwordLayout, passwordEditText.text.toString().trim()
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
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun handleLogin(
        emailLayout: TextInputLayout, email: String,
        passwordLayout: TextInputLayout, password: String
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

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    statusTextView.text = getString(R.string.success_login)

                    // Obtener datos del usuario desde Firestore
                    val currentUser = auth.currentUser
                    if (currentUser != null) {
                        firestore.collection("users").document(currentUser.uid).get()
                            .addOnSuccessListener { document ->
                                if (document != null && document.exists()) {
                                    // Obtener los datos del documento
                                    val nombres = document.getString("nombres") ?: "Nombre"
                                    val apellidos = document.getString("apellidos") ?: "Apellido"
                                    val edad = document.getLong("edad")?.toInt() ?: 0
                                    val nivel = document.getLong("nivel")?.toInt() ?: 0 // Obtener nivel

                                    // Crear el objeto Usuario con nivel
                                    val usuario = Usuario(currentUser.uid, nombres, apellidos, edad, nivel)

                                    // Guardar el objeto Usuario en SharedPreferences
                                    guardarUsuarioEnSharedPreferences(usuario)

                                    // Navegar a MainActivity
                                    val intent = Intent(this, MainActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                } else {
                                    statusTextView.text = "Error: Usuario no encontrado"
                                }
                            }
                            .addOnFailureListener { e ->
                                statusTextView.text = "Error al cargar datos: ${e.message}"
                            }
                    }
                } else {
                    val exception = task.exception
                    val errorMessage = when {
                        exception?.message?.contains("There is no user record") == true -> getString(R.string.error_no_user)
                        exception?.message?.contains("The password is invalid") == true -> getString(R.string.error_wrong_password)
                        else -> getString(R.string.error_login_failure, exception?.message)
                    }
                    statusTextView.text = errorMessage
                }
            }
    }

    // Modificar para incluir nivel
    private fun guardarUsuarioEnSharedPreferences(usuario: Usuario) {
        val sharedPreferences = getSharedPreferences("user_data", MODE_PRIVATE) // Cambia el nombre aquí
        val editor = sharedPreferences.edit()
        editor.putString("user_id", usuario.id)
        editor.putString("user_nombres", usuario.nombres) // Manten la clave coherente
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
