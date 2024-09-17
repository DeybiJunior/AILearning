package com.dapm.ailearning.Login

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.dapm.ailearning.MainActivity
import com.dapm.ailearning.R
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.firestore.FirebaseFirestore
class InicioSesionActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var statusTextView: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_iniciosesion)

        firestore = FirebaseFirestore.getInstance()
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
            // Lógica para continuar sin iniciar sesión
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

        firestore.collection("users")
            .whereEqualTo("email", email)
            .whereEqualTo("password", password)
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    statusTextView.text = getString(R.string.error_login)
                } else {
                    statusTextView.text = getString(R.string.success_login)

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
            }
            .addOnFailureListener { e ->
                statusTextView.text = getString(R.string.error_login_failure, e.message)
            }
    }
}