package com.dapm.ailearning.Perfil

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.dapm.ailearning.R
import com.google.android.material.button.MaterialButtonToggleGroup

import android.animation.ObjectAnimator
import android.view.animation.AccelerateDecelerateInterpolator


class SeleccionDificultadActivity : AppCompatActivity() {

    private lateinit var toggleGroup: MaterialButtonToggleGroup
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var btnBack: ImageButton
    private lateinit var txtNombreUsuario: TextView
    private lateinit var imageViewDificultad: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seleccion_dificultad)
        btnBack = findViewById(R.id.btnBack)
        toggleGroup = findViewById(R.id.toggleButton)
        txtNombreUsuario = findViewById(R.id.txtNombreUsuario) // Asegúrate de que este ID esté en tu XML
        imageViewDificultad = findViewById(R.id.imageViewDificultad)

        // Inicializar SharedPreferences
        sharedPreferences = getSharedPreferences("user_data", Context.MODE_PRIVATE)

        // Recuperar o crear usuario
        var nombreUsuario = sharedPreferences.getString("user_nombres", null) ?: "Usuario Offline"
        txtNombreUsuario.text = "Hola, $nombreUsuario"

        // Configurar el botón de regreso
        btnBack.setOnClickListener {
            finish()
        }

        // Asignar el color inicial basado en la dificultad guardada
        val savedDificultad = sharedPreferences.getString("dificultad", "Básico")
        applyButtonColor(savedDificultad)

        // Cambiar la imagen según la dificultad
        when (savedDificultad) {
            "Básico" -> {
                imageViewDificultad.setImageResource(R.drawable.basico)
            }
            "Intermedio" -> {
                imageViewDificultad.setImageResource(R.drawable.intermedio)
            }
            "Avanzado" -> {
                imageViewDificultad.setImageResource(R.drawable.aprovado)
            }
            else -> {
                imageViewDificultad.setImageResource(R.drawable.basico) // Imagen por defecto
            }
        }

        // Guardar la dificultad seleccionada en SharedPreferences y aplicar colores al cambiar
        toggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                val dificultad = when (checkedId) {
                    R.id.buttonBasico -> "Básico"
                    R.id.buttonIntermedio -> "Intermedio"
                    R.id.buttonAvanzado -> "Avanzado"
                    else -> "Básico"
                }

                // Usar animación para cambiar la imagen
                val fadeOut = ObjectAnimator.ofFloat(imageViewDificultad, "alpha", 1f, 0f)
                fadeOut.duration = 300
                fadeOut.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        when (dificultad) {
                            "Básico" -> {
                                imageViewDificultad.setImageResource(R.drawable.basico)
                            }
                            "Intermedio" -> {
                                imageViewDificultad.setImageResource(R.drawable.intermedio)
                            }
                            "Avanzado" -> {
                                imageViewDificultad.setImageResource(R.drawable.aprovado)
                            }
                        }
                        // Fade in the new image
                        val fadeIn = ObjectAnimator.ofFloat(imageViewDificultad, "alpha", 0f, 1f)
                        fadeIn.duration = 300
                        fadeIn.start()
                    }
                })
                fadeOut.start()

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
                // Activar solo el botón Básico
                basicoButton.setBackgroundColor(ContextCompat.getColor(this, R.color.basicoColor))
                basicoButton.setTextColor(ContextCompat.getColor(this, R.color.blancosiempre))
                intermedioButton.setTextColor(defaultTextColor)
                avanzadoButton.setTextColor(defaultTextColor)
            }
            "Intermedio" -> {
                intermedioButton.setBackgroundColor(ContextCompat.getColor(this, R.color.intermedioColor))
                intermedioButton.setTextColor(ContextCompat.getColor(this, R.color.blancosiempre))
                basicoButton.setTextColor(defaultTextColor)
                avanzadoButton.setTextColor(defaultTextColor)
            }
            "Avanzado" -> {
                avanzadoButton.setBackgroundColor(ContextCompat.getColor(this, R.color.avanzadoColor))
                avanzadoButton.setTextColor(ContextCompat.getColor(this, R.color.blancosiempre))
                basicoButton.setTextColor(defaultTextColor)
                intermedioButton.setTextColor(defaultTextColor)

            }
        }
    }


    private fun changeImageWithAnimation(dificultad: String) {
        val imageRes = when (dificultad) {
            "Básico" -> R.drawable.basico
            "Intermedio" -> R.drawable.intermedio
            "Avanzado" -> R.drawable.aprovado
            else -> R.drawable.basico // Imagen por defecto
        }

        // Realiza una animación para un cambio suave de la imagen
        val fadeOut = ObjectAnimator.ofFloat(imageViewDificultad, "alpha", 1f, 0f)
        fadeOut.duration = 300 // Duración del fadeOut (300ms)

        fadeOut.start()

        // Después de que la animación de fadeOut termina, cambiamos la imagen
        fadeOut.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                imageViewDificultad.setImageResource(imageRes)

                // Iniciar fadeIn (mostrar la nueva imagen con una animación suave)
                val fadeIn = ObjectAnimator.ofFloat(imageViewDificultad, "alpha", 0f, 1f)
                fadeIn.duration = 300 // Duración del fadeIn (300ms)
                fadeIn.start()
            }
        })
    }
}
