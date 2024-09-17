package com.dapm.ailearning.Login

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dapm.ailearning.MainActivity
import com.dapm.ailearning.R
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.firestore.FirebaseFirestore

class RegistroActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)

        firestore = FirebaseFirestore.getInstance()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        val nombresLayout = findViewById<TextInputLayout>(R.id.nombresLayout)
        val apellidosLayout = findViewById<TextInputLayout>(R.id.apellidosLayout)
        val edadLayout = findViewById<TextInputLayout>(R.id.edadLayout)
        val emailLayout = findViewById<TextInputLayout>(R.id.emailLayout)
        val passwordLayout = findViewById<TextInputLayout>(R.id.passwordLayout)
        val confirmPasswordLayout = findViewById<TextInputLayout>(R.id.confirmPasswordLayout)

        val CloseRegistro = findViewById<ImageButton>(R.id.CloseRegistroLayaut)
        val nombresEditText = findViewById<EditText>(R.id.nombresEditText)
        val apellidosEditText = findViewById<EditText>(R.id.apellidosEditText)
        val edadEditText = findViewById<EditText>(R.id.edadEditText)
        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val confirmPasswordEditText = findViewById<EditText>(R.id.confirmPasswordEditText)

        val registerButton = findViewById<Button>(R.id.registerButton)
        CloseRegistro.setOnClickListener {
            val intent = Intent(this, InicioSesionActivity::class.java)
            startActivity(intent)
        }
        registerButton.setOnClickListener {
            handleRegister(
                nombresLayout, nombresEditText.text.toString().trim(),
                apellidosLayout, apellidosEditText.text.toString().trim(),
                edadLayout, edadEditText.text.toString().trim(),
                emailLayout, emailEditText.text.toString().trim(),
                passwordLayout, passwordEditText.text.toString().trim(),
                confirmPasswordLayout, confirmPasswordEditText.text.toString().trim()
            )
        }
    }

    private fun handleRegister(
        nombresLayout: TextInputLayout, nombres: String,
        apellidosLayout: TextInputLayout, apellidos: String,
        edadLayout: TextInputLayout, edadStr: String,
        emailLayout: TextInputLayout, email: String,
        passwordLayout: TextInputLayout, password: String,
        confirmPasswordLayout: TextInputLayout, confirmPassword: String
    ) {
        val edad = edadStr.toIntOrNull()

        // Limpiar errores previos
        nombresLayout.error = null
        apellidosLayout.error = null
        edadLayout.error = null
        emailLayout.error = null
        passwordLayout.error = null
        confirmPasswordLayout.error = null

        // Validación de los campos
        if (nombres.isEmpty()) {
            nombresLayout.error = getString(R.string.error_nombres)
            return
        }

        if (apellidos.isEmpty()) {
            apellidosLayout.error = getString(R.string.error_apellidos)
            return
        }

        if (edad == null || edad <= 0 || edad >= 100) {
            edadLayout.error = getString(R.string.error_edad)
            return
        }

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.error = getString(R.string.error_email)
            return
        }

        val passwordValidationResult = validatePassword(password)
        if (passwordValidationResult != null) {
            passwordLayout.error = passwordValidationResult
            return
        }

        if (password != confirmPassword) {
            confirmPasswordLayout.error = getString(R.string.error_password_mismatch)
            return
        }

        // Crear el usuario
        val user = hashMapOf(
            "nombres" to nombres,
            "apellidos" to apellidos,
            "edad" to edad,
            "email" to email,
            "password" to password
        )

        // Guardar el usuario en Firestore
        firestore.collection("users")
            .add(user)
            .addOnSuccessListener { documentReference ->
                // Registro exitoso
                Toast.makeText(this, "Usuario registrado", Toast.LENGTH_SHORT).show()

                // Guardar en SharedPreferences que ya no es la primera vez
                val sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.putBoolean("isFirstTime", false)
                editor.apply()

                // Redirigir al MainActivity
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al registrar usuario: $e", Toast.LENGTH_SHORT).show()
            }
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

