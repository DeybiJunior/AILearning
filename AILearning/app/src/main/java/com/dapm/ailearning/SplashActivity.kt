package com.dapm.ailearning

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.dapm.ailearning.Login.InicioSesionActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Verificar SharedPreferences para ver si el usuario ya inició sesión o pasó por el login
        val sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        val isFirstTime = sharedPreferences.getBoolean("isFirstTime", true)

        if (isFirstTime) {
            // Si es la primera vez, mostrar el LoginActivity
            val intent = Intent(this, InicioSesionActivity::class.java)
            startActivity(intent)
        } else {
            // Si ya pasó por el login, mostrar directamente el MainActivity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        // Finalizar SplashActivity
        finish()
    }
}
