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
import com.google.firebase.auth.FirebaseAuth

class InicioSesionActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var statusTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_iniciosesion)

        auth = FirebaseAuth.getInstance()
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

                    val sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
                    val editor = sharedPreferences.edit()
                    editor.putBoolean("isFirstTime", false)
                    editor.apply()

                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
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

}
