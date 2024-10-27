package com.dapm.ailearning.Perfil

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.dapm.ailearning.R
import com.google.android.material.button.MaterialButtonToggleGroup

 class SeleccionDificultadActivity : AppCompatActivity() {

     private lateinit var toggleGroup: MaterialButtonToggleGroup
     private lateinit var sharedPreferences: SharedPreferences
     private lateinit var btnBack: ImageButton
     private lateinit var txtNombreUsuario: TextView

     override fun onCreate(savedInstanceState: Bundle?) {
         super.onCreate(savedInstanceState)
         setContentView(R.layout.activity_seleccion_dificultad)
         btnBack = findViewById(R.id.btnBack)
         toggleGroup = findViewById(R.id.toggleButton)
         txtNombreUsuario = findViewById(R.id.txtNombreUsuario) // Asegúrate de que este ID esté en tu XML

            // Inicializar SharedPreferences
         sharedPreferences = getSharedPreferences("user_data", Context.MODE_PRIVATE)

            // Recuperar o crear usuario
         var nombreUsuario = sharedPreferences.getString("user_nombres", null)

         if (nombreUsuario == null) {
                // Si no hay un usuario guardado, crear un usuario offline
             guardarUsuarioOfflineEnSharedPreferences()
             nombreUsuario = sharedPreferences.getString("user_nombres", "Usuario Offline")
            }

         txtNombreUsuario.text = "Hola, ${nombreUsuario ?: "Usuario Offline"}"

            // Configurar el botón de regreso
         btnBack.setOnClickListener {
                finish()
            }

            // Asignar el color inicial basado en la dificultad guardada
         val savedDificultad = sharedPreferences.getString("dificultad", "Básico")
         applyButtonColor(savedDificultad)

            // Guardar la dificultad seleccionada en SharedPreferences y aplicar colores al cambiar
         toggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
             if (isChecked) {
                 val dificultad = when (checkedId) {
                     R.id.buttonBasico -> "Básico"
                     R.id.buttonIntermedio -> "Intermedio"
                     R.id.buttonAvanzado -> "Avanzado"
                     else -> "Básico"
                 }

                 // Guardar en SharedPreferences
                 with(sharedPreferences.edit()) {
                     putString("dificultad", dificultad)
                     apply()
                 }

                 applyButtonColor(dificultad)
                }
            }
        }
    private fun guardarUsuarioOfflineEnSharedPreferences() {
        // Guardar datos de usuario offline en SharedPreferences
        val editor = sharedPreferences.edit()
        editor.putString("user_nombres", "Usuario Offline")
        editor.apply()
    }


     private fun applyButtonColor(dificultad: String?) {
         val basicoButton = findViewById<Button>(R.id.buttonBasico)
         val intermedioButton = findViewById<Button>(R.id.buttonIntermedio)
         val avanzadoButton = findViewById<Button>(R.id.buttonAvanzado)

         // Restablecer colores
         val defaultTextColor = ContextCompat.getColor(this, R.color.md_theme_onSurface) // Color del texto por defecto
         basicoButton.setBackgroundColor(ContextCompat.getColor(this, R.color.md_theme_background))
         intermedioButton.setBackgroundColor(ContextCompat.getColor(this, R.color.md_theme_background))
         avanzadoButton.setBackgroundColor(ContextCompat.getColor(this, R.color.md_theme_background))

         // Cambiar el color del botón y del texto según la dificultad seleccionada
         when (dificultad) {
             "Básico" -> {
                 basicoButton.setBackgroundColor(ContextCompat.getColor(this, R.color.basicoColor))
                 basicoButton.setTextColor(ContextCompat.getColor(this, R.color.blancosiempre)) // Cambiar a blanco
                 intermedioButton.setTextColor(defaultTextColor) // Restablecer texto
                 avanzadoButton.setTextColor(defaultTextColor) // Restablecer texto
             }
             "Intermedio" -> {
                 intermedioButton.setBackgroundColor(ContextCompat.getColor(this, R.color.intermedioColor))
                 intermedioButton.setTextColor(ContextCompat.getColor(this, R.color.blancosiempre)) // Cambiar a blanco
                 basicoButton.setTextColor(defaultTextColor) // Restablecer texto
                 avanzadoButton.setTextColor(defaultTextColor) // Restablecer texto
             }
             "Avanzado" -> {
                 avanzadoButton.setBackgroundColor(ContextCompat.getColor(this, R.color.avanzadoColor))
                 avanzadoButton.setTextColor(ContextCompat.getColor(this, R.color.blancosiempre)) // Cambiar a blanco
                 basicoButton.setTextColor(defaultTextColor) // Restablecer texto
                 intermedioButton.setTextColor(defaultTextColor) // Restablecer texto
             }
         }
     }
 }