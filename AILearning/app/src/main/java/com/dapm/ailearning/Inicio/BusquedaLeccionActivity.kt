package com.dapm.ailearning.Inicio

import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.RelativeSizeSpan
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dapm.ailearning.Datos.Leccion
import com.dapm.ailearning.Datos.LeccionViewModel
import com.dapm.ailearning.R
import kotlin.properties.Delegates

class BusquedaLeccionActivity : AppCompatActivity() {

    private lateinit var leccionViewModel: LeccionViewModel

    // Colores
    private var colorSeleccionado by Delegates.notNull<Int>()
    private var colorNoSeleccionado by Delegates.notNull<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_busqueda_leccion)

        // Obtener colores desde resources
        colorSeleccionado = ContextCompat.getColor(this, R.color.tipo_seleccionado)
        colorNoSeleccionado = ContextCompat.getColor(this, R.color.md_theme_primary)


        // Inicializar el ViewModel
        leccionViewModel = ViewModelProvider(this).get(LeccionViewModel::class.java)

        // Obtener el userId desde SharedPreferences
        val sharedPreferences = getSharedPreferences("user_data", MODE_PRIVATE)
        val userId = sharedPreferences.getString("user_id", null)
        val nivel = sharedPreferences.getInt("user_nivel", 0)

        // Determinar la dificultad según el nivel
        val dificultad = when (nivel) {
            0 -> "Básico"
            1 -> "Intermedio"
            2 -> "Avanzado"
            else -> "Básico" // En caso de que el nivel sea diferente de 0, 1 o 2
        }

        // Obtener el tema desde SharedPreferences
        val sharedPreferences2 = getSharedPreferences("tema", MODE_PRIVATE)
        val tema = sharedPreferences2.getString("temaEstudio", null)

        var tipo = ""
        // Configurar los botones de tipo
        val btn1 = findViewById<Button>(R.id.btn1)
        val btn2 = findViewById<Button>(R.id.btn2)
        val btn3 = findViewById<Button>(R.id.btn3)
        val btn4 = findViewById<Button>(R.id.btn4)

        // Función para restablecer textos y colores de los botones
        fun resetButtons() {
            btn1.text = getString(R.string.tipo_1)
            btn2.text = getString(R.string.tipo_2)
            btn3.text = getString(R.string.tipo_3)
            btn4.text = getString(R.string.tipo_4)

            btn1.setBackgroundColor(colorNoSeleccionado)
            btn2.setBackgroundColor(colorNoSeleccionado)
            btn3.setBackgroundColor(colorNoSeleccionado)
            btn4.setBackgroundColor(colorNoSeleccionado)
        }

        // Asignar valores a tipo según el botón clickeado y mostrar descripción adicional
        btn1.setOnClickListener {
            resetButtons() // Restablece los textos y colores antes de cambiar
            tipo = getString(R.string.tipo_1) // Asigna el valor correspondiente

            // Texto principal y descripción
            val textoPrincipal = getString(R.string.tipo_1)
            val descripcion = getString(R.string.desc_tipo_1)

            // Crear SpannableString para cambiar el tamaño de la descripción
            val spannable = SpannableString(textoPrincipal + "\n\n" + descripcion)
            spannable.setSpan(RelativeSizeSpan(0.8f), textoPrincipal.length, spannable.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

            btn1.text = spannable
            btn1.setBackgroundColor(colorSeleccionado) // Cambia el color del botón seleccionado
            Toast.makeText(this, "Seleccionaste: $textoPrincipal", Toast.LENGTH_SHORT).show()
        }
        btn2.setOnClickListener {
            resetButtons() // Restablece los textos y colores antes de cambiar
            tipo = getString(R.string.tipo_2) // Asigna el valor correspondiente

            // Texto principal y descripción
            val textoPrincipal = getString(R.string.tipo_2)
            val descripcion = getString(R.string.desc_tipo_2)

            // Crear SpannableString para cambiar el tamaño de la descripción
            val spannable = SpannableString(textoPrincipal + "\n\n" + descripcion)
            spannable.setSpan(RelativeSizeSpan(0.8f), textoPrincipal.length, spannable.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

            btn2.text = spannable
            btn2.setBackgroundColor(colorSeleccionado) // Cambia el color del botón seleccionado
            Toast.makeText(this, "Seleccionaste: $textoPrincipal", Toast.LENGTH_SHORT).show()
        }
        btn3.setOnClickListener {
            resetButtons() // Restablece los textos y colores antes de cambiar
            tipo = getString(R.string.tipo_3) // Asigna el valor correspondiente

            // Texto principal y descripción
            val textoPrincipal = getString(R.string.tipo_3)
            val descripcion = getString(R.string.desc_tipo_3)

            // Crear SpannableString para cambiar el tamaño de la descripción
            val spannable = SpannableString(textoPrincipal + "\n\n" + descripcion)
            spannable.setSpan(RelativeSizeSpan(0.8f), textoPrincipal.length, spannable.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

            btn3.text = spannable
            btn3.setBackgroundColor(colorSeleccionado) // Cambia el color del botón seleccionado
            Toast.makeText(this, "Seleccionaste: $textoPrincipal", Toast.LENGTH_SHORT).show()
        }
        btn4.setOnClickListener {
            resetButtons() // Restablece los textos y colores antes de cambiar
            tipo = getString(R.string.tipo_4) // Asigna el valor correspondiente

            // Texto principal y descripción
            val textoPrincipal = getString(R.string.tipo_4)
            val descripcion = getString(R.string.desc_tipo_4)

            // Crear SpannableString para cambiar el tamaño de la descripción
            val spannable = SpannableString(textoPrincipal + "\n\n" + descripcion)
            spannable.setSpan(RelativeSizeSpan(0.8f), textoPrincipal.length, spannable.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

            btn4.text = spannable
            btn4.setBackgroundColor(colorSeleccionado) // Cambia el color del botón seleccionado
            Toast.makeText(this, "Seleccionaste: $textoPrincipal", Toast.LENGTH_SHORT).show()
        }

        // Botón para agregar una nueva lección
        val btnAgregarLeccion = findViewById<Button>(R.id.btnAgregarLeccion)
        btnAgregarLeccion.setOnClickListener {
            if (userId != null) {
                // Verificar que tipo no esté vacío
                if (tipo.isEmpty()) {
                    Toast.makeText(this, "Debes seleccionar un tipo de lección", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener // Salir del onClick si tipo está vacío
                }
                // Crear una nueva lección con datos ficticios por ahora
                val nuevaLeccion = Leccion(
                    userId = userId,
                    tipo = tipo,
                    dificultad = dificultad,
                    tema = "$tema",
                    json = "{\"pregunta\":\"¿Qué animal es este?\"}"
                )

                // Insertar la lección en la base de datos
                leccionViewModel.insert(nuevaLeccion)
                Toast.makeText(this, "Lección agregada", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Log.e("BusquedaLeccionActivity", "userId es nulo, no se puede agregar la lección.")
            }
        }

        // Configurar el botón de regresar
        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        btnBack.setOnClickListener {
            val intent = Intent(this, SeleccionTemaActivity::class.java)
            startActivity(intent)
            finish()  // Cierra BusquedaLeccionActivity para que no esté en la pila
        }
    }
}
