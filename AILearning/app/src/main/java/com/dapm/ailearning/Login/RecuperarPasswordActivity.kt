package com.dapm.ailearning.Login

import android.content.Context
import android.os.Bundle
import android.os.SystemClock
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dapm.ailearning.R
import com.google.firebase.auth.FirebaseAuth

class RecuperarPasswordActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val maxAttempts = 3
    private val lockoutDuration = 3600000L // 1 hour in milliseconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recuperar_password)

        auth = FirebaseAuth.getInstance()

        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val enviarButton = findViewById<Button>(R.id.enviarButton)
        val regresarLogin = findViewById<TextView>(R.id.regresarLogin)
        val closeRegistroLayaut = findViewById<ImageButton>(R.id.CloseRegistroLayaut)

        // Recuperar intentos previos y tiempo de bloqueo de SharedPreferences
        val prefs = getSharedPreferences("RecuperarPasswordPrefs", Context.MODE_PRIVATE)
        var attemptCount = prefs.getInt("attemptCount", 0)
        val lockoutTime = prefs.getLong("lockoutTime", 0)

        // Deshabilita el botón inicialmente
        enviarButton.isEnabled = false

        // Agrega un TextWatcher para habilitar el botón cuando ambos campos estén completos
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Habilita el botón si ambos campos están completos
                enviarButton.isEnabled = emailEditText.text.isNotEmpty()
            }
            override fun afterTextChanged(s: Editable?) {}
        }

        // Añade el TextWatcher a ambos EditText
        emailEditText.addTextChangedListener(textWatcher)


        // Revisar si el tiempo de bloqueo ha terminado
        if (SystemClock.elapsedRealtime() < lockoutTime) {
            disableButton(enviarButton)
        } else {
            resetAttempts(prefs)
        }

        enviarButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            if (email.isNotEmpty()) {
                // Verificar si se alcanzó el límite de intentos
                if (attemptCount < maxAttempts) {
                    auth.fetchSignInMethodsForEmail(email)
                        .addOnCompleteListener { fetchTask ->
                            if (fetchTask.isSuccessful) {
                                val signInMethods = fetchTask.result?.signInMethods
                                if (signInMethods.isNullOrEmpty()) {
                                    Toast.makeText(this, "El correo ingresado no está registrado", Toast.LENGTH_SHORT).show()
                                } else {
                                    auth.sendPasswordResetEmail(email)
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                Toast.makeText(this, "Correo de recuperación enviado, verifica tu correo electrónico", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                }
                            } else {
                                Toast.makeText(this, "Error al verificar el correo", Toast.LENGTH_SHORT).show()
                            }
                        }

                    // Incrementar el contador de intentos
                    attemptCount++
                    prefs.edit().putInt("attemptCount", attemptCount).apply()

                    // Bloquear si se alcanzaron los 3 intentos
                    if (attemptCount >= maxAttempts) {
                        val newLockoutTime = SystemClock.elapsedRealtime() + lockoutDuration
                        prefs.edit().putLong("lockoutTime", newLockoutTime).apply()
                        disableButton(enviarButton)
                    }
                } else {
                    Toast.makeText(this, "Límite de intentos alcanzado. Intenta nuevamente en una hora.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Por favor, ingresa un correo electrónico", Toast.LENGTH_SHORT).show()
            }
        }


        // Acción para retroceder al inicio de sesión
        regresarLogin.setOnClickListener {
            finish()
        }

        closeRegistroLayaut.setOnClickListener {
            finish()
        }
    }

    private fun disableButton(button: Button) {
        button.isEnabled = false
        Toast.makeText(this, "Has alcanzado el límite de intentos. Intenta nuevamente en una hora.", Toast.LENGTH_LONG).show()
    }

    private fun resetAttempts(prefs: android.content.SharedPreferences) {
        prefs.edit().putInt("attemptCount", 0).apply()
        prefs.edit().putLong("lockoutTime", 0).apply()
    }
}
