package com.dapm.ailearning.Login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dapm.ailearning.Datos.AppDatabase
import com.dapm.ailearning.Datos.Usuario
import com.dapm.ailearning.MainActivity
import com.dapm.ailearning.R
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RegistroActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var checkBoxTerminos: CheckBox
    private lateinit var database: AppDatabase
    private lateinit var sectionAutoCompleteTextView: MaterialAutoCompleteTextView
    private lateinit var gradeAutoCompleteTextView: MaterialAutoCompleteTextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        database = AppDatabase.getDatabase(applicationContext)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        val nombresLayout = findViewById<TextInputLayout>(R.id.nombresLayout)
        val apellidosLayout = findViewById<TextInputLayout>(R.id.apellidosLayout)
        val edadLayout = findViewById<TextInputLayout>(R.id.edadLayout)
        val emailLayout = findViewById<TextInputLayout>(R.id.emailLayout)
        val passwordLayout = findViewById<TextInputLayout>(R.id.passwordLayout)
        val confirmPasswordLayout = findViewById<TextInputLayout>(R.id.confirmPasswordLayout)
        val sectionLayout = findViewById<TextInputLayout>(R.id.seccionLayout)
        val gradeLayout = findViewById<TextInputLayout>(R.id.gradoLayout)

        val CloseRegistro = findViewById<ImageButton>(R.id.CloseRegistroLayaut)
        val nombresEditText = findViewById<EditText>(R.id.nombresEditText)
        val apellidosEditText = findViewById<EditText>(R.id.apellidosEditText)
        val edadEditText = findViewById<EditText>(R.id.edadEditText)
        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val confirmPasswordEditText = findViewById<EditText>(R.id.confirmPasswordEditText)

        sectionAutoCompleteTextView = findViewById(R.id.seccionAutoCompleteTextView)
        gradeAutoCompleteTextView = findViewById(R.id.gradoAutoCompleteTextView)


        val registerButton = findViewById<Button>(R.id.registerButton)

        //TERMINOS Y CONDICIONES
        // Inicializa el CheckBox
        checkBoxTerminos = findViewById(R.id.checkBoxTerminos)

        // Configura el click en los términos y condiciones
        val txtTerminos = findViewById<TextView>(R.id.txtTerminos)
        txtTerminos.setOnClickListener {
            // Abre el DialogFragment de términos y condiciones
            val dialog = TerminosDialogFragment {
                // Si acepta, se marca el CheckBox
                checkBoxTerminos.isChecked = true
            }
            // Muestra el DialogFragment desde la Activity
            dialog.show(supportFragmentManager, "TerminosDialogFragment")
        }

        val sections = resources.getStringArray(R.array.sections)  // Obtiene las secciones desde strings.xml
        val grades = resources.getStringArray(R.array.grades)      // Obtiene los grados desde strings.xml


        val sectionAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, sections)
        sectionAutoCompleteTextView.setAdapter(sectionAdapter)

        val gradeAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, grades)
        gradeAutoCompleteTextView.setAdapter(gradeAdapter)


        //REGISTRO
        CloseRegistro.setOnClickListener {
            val intent = Intent(this, InicioSesionActivity::class.java)
            startActivity(intent)
        }
        registerButton.setOnClickListener {
            if (checkBoxTerminos.isChecked) {
                handleRegister(
                    nombresLayout, nombresEditText.text.toString().trim(),
                    apellidosLayout, apellidosEditText.text.toString().trim(),
                    edadLayout, edadEditText.text.toString().trim(),
                    emailLayout, emailEditText.text.toString().trim(),
                    sectionLayout, sectionAutoCompleteTextView.text.toString().trim(), // <-- Aquí
                    gradeLayout, gradeAutoCompleteTextView.text.toString().trim(), // <-- Y aquí
                    passwordLayout, passwordEditText.text.toString().trim(),
                    confirmPasswordLayout, confirmPasswordEditText.text.toString().trim()
                )
            } else{
                Toast.makeText(this, "Debes aceptar los términos y condiciones", Toast.LENGTH_SHORT).show()
            }
        }
    }



    private fun handleRegister(
        nombresLayout: TextInputLayout, nombres: String,
        apellidosLayout: TextInputLayout, apellidos: String,
        edadLayout: TextInputLayout, edadStr: String,
        emailLayout: TextInputLayout, email: String,
        sectionLayout: TextInputLayout, section: String,
        gradeLayout: TextInputLayout, grade: String,
        passwordLayout: TextInputLayout, password: String,
        confirmPasswordLayout: TextInputLayout, confirmPassword: String
    ) {
        val edad = edadStr.toIntOrNull()

        nombresLayout.error = null
        apellidosLayout.error = null
        edadLayout.error = null
        emailLayout.error = null
        sectionLayout.error = null
        gradeLayout.error = null
        passwordLayout.error = null
        confirmPasswordLayout.error = null

// Validación de nombres
        if (nombres.isEmpty()) {
            nombresLayout.error = getString(R.string.error_nombres)
            return
        } else if (nombres.length < 3 || nombres.length > 50) {
            nombresLayout.error = getString(R.string.error_min_length) // Mensaje de error para longitud mínima
            return
        } else if (!nombres.matches(Regex("^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$"))) {
            nombresLayout.error = getString(R.string.error_invalid_characters) // Mensaje de error para caracteres inválidos
            return
        }

        if (apellidos.isEmpty()) {
            apellidosLayout.error = getString(R.string.error_apellidos)
            return
        } else if (apellidos.length < 3 || apellidos.length > 50) {
            apellidosLayout.error = getString(R.string.error_min_length) // Mensaje de error para longitud mínima
            return
        } else if (!apellidos.matches(Regex("^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$"))) {
            apellidosLayout.error = getString(R.string.error_invalid_characters) // Mensaje de error para caracteres inválidos
            return
        }

        if (edad == null || edad <= 0 || edad >= 100) {
            edadLayout.error = getString(R.string.error_edad)
            return
        }
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.error = getString(R.string.error_email)
            return
        } else if (!email.matches(Regex("^[a-zA-Z0-9._%+-]{2,}@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$"))) {
            emailLayout.error = getString(R.string.error_email)
            return
        }


// Validación para la sección
        val sectionValue = sectionAutoCompleteTextView.text.toString().trim()
        if (sectionValue.isEmpty()) {
            sectionLayout.error = getString(R.string.error_section)  // Asegúrate de tener este mensaje en strings.xml
            return
        } else {
            sectionLayout.error = null  // Limpiar el error si la validación es exitosa
        }

// Validación para el grado
        val gradeValue = gradeAutoCompleteTextView.text.toString().trim()
        if (gradeValue.isEmpty()) {
            gradeLayout.error = getString(R.string.error_grade)  // Asegúrate de tener este mensaje en strings.xml
            return
        } else {
            gradeLayout.error = null  // Limpiar el error si la validación es exitosa
        }

// Aquí puedes seguir con el resto de las validaciones de los otros campos



        val passwordValidationResult = validatePassword(password)
        if (passwordValidationResult != null) {
            passwordLayout.error = passwordValidationResult
            return
        }

        if (password != confirmPassword) {
            confirmPasswordLayout.error = getString(R.string.error_password_mismatch)
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Usuario registrado", Toast.LENGTH_SHORT).show()

                    val nivel = 0 // Puedes cambiar este valor según la lógica de tu aplicación

                    // Guarda los datos del usuario en Firestore
                    val user = hashMapOf(
                        "nombres" to nombres,
                        "apellidos" to apellidos,
                        "edad" to edad,
                        "nivel" to nivel,
                        "seccion" to section,
                        "grado" to grade
                    )


                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        firestore.collection("users")
                            .document(userId)
                            .set(user)
                            .addOnSuccessListener {
                                guardarUsuarioEnSharedPreferences(Usuario(userId, nombres, apellidos, edad, nivel, section, grade))

                                // Redirigir al usuario a MainActivity
                                val intent = Intent(this, MainActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Error al guardar datos del usuario en Firestore: $e", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    Toast.makeText(this, "Error al registrar usuario: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }



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
                "Nivel: ${usuario.nivel}" +
                "Edad: ${usuario.seccion}, " +
        "Nivel: ${usuario.grado}")
    }

    private fun validatePassword(password: String): String? {
        if (password.length < 8) {
            return getString(R.string.error_password_too_short)
        }

        val hasUpperCase = password.any { it.isUpperCase() }
        val hasLowerCase = password.any { it.isLowerCase() }
        val hasNumber = password.any { it.isDigit() }

        return when {
            !hasUpperCase -> getString(R.string.error_password_no_uppercase)
            !hasLowerCase -> getString(R.string.error_password_no_lowercase)
            !hasNumber -> getString(R.string.error_password_no_number)
            else -> null
        }
    }


}
