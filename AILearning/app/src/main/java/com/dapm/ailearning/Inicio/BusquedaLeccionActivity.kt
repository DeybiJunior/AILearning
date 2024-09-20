package com.dapm.ailearning.Inicio

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.dapm.ailearning.MainActivity
import com.dapm.ailearning.R

class BusquedaLeccionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_busqueda_leccion)

        val btnGuardar = findViewById<Button>(R.id.btnGuardar)
        val btnBack = findViewById<ImageButton>(R.id.btnBack)

        // Configura el botón Guardar
        btnGuardar.setOnClickListener {
            // Regresa al InicioFragment
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // Cierra la actividad actual
        }

        // Configura el botón Back
        btnBack.setOnClickListener {
            finish() // Cierra la actividad actual
        }
    }
}