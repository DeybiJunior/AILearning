package com.dapm.ailearning.Inicio

import android.app.ProgressDialog
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
import com.dapm.ailearning.SolicituLocal.ApiService
import kotlin.properties.Delegates

class BusquedaLeccionActivity : AppCompatActivity() {

    private lateinit var leccionViewModel: LeccionViewModel

    // Colores
    private var colorSeleccionado by Delegates.notNull<Int>()
    private var colorNoSeleccionado by Delegates.notNull<Int>()
    val prompts = mutableMapOf<String, String>()

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
        val btn5 = findViewById<Button>(R.id.btn5)


        // Función para restablecer textos y colores de los botones
        fun resetButtons() {
            btn1.text = getString(R.string.tipo_1)
            btn2.text = getString(R.string.tipo_2)
            btn3.text = getString(R.string.tipo_3)
            btn4.text = getString(R.string.tipo_4)
            btn5.text = getString(R.string.tipo_5)

            btn1.setBackgroundColor(colorNoSeleccionado)
            btn2.setBackgroundColor(colorNoSeleccionado)
            btn3.setBackgroundColor(colorNoSeleccionado)
            btn4.setBackgroundColor(colorNoSeleccionado)
            btn4.setBackgroundColor(colorNoSeleccionado)
            btn5.setBackgroundColor(colorNoSeleccionado)


        }
        prompts[getString(R.string.tipo_1)] = "Genera un JSON con una lista de 10 frases en inglés de nivel básico $dificultad sobre $tema. La estructura debe ser: [{ \"ID\": <número>, \"frase\": \"<String>\" }, ...]"
        prompts[getString(R.string.tipo_2)] = """Generar un JSON con 1 lectura corta (máximo 50 palabras, en inglés) sobre $tema para una dificultad básica de $dificultad, todo el textto en idioma inglés. Realizar un cuestionario sobre lectura que conste de 3 preguntas de opción múltiple con 4 opciones y 1 respuesta correcta.  La estructura debe ser: [{"ID": <number>, "reading": "<String>", "quiz": [{"question": "<String>", "options": ["<option 1>", "<option 2>", "<option 3>", "<option 4>"], "correct_answer": "<option n>"}]}]"""
        prompts[getString(R.string.tipo_3)] = """Genera un JSON con 1 lectura corta (max 50 palabras) en Ingles sobre $tema de nivel básico - $dificultad. Hacer un cuestionario sobre lectura que conste de 3 preguntas de opción múltiple con 4 opciones y 1 respuesta correcta. Estructura de Ejemplo: [{"ID": <number>, "reading": "<String>", "quiz": [{"question": "<String>", "options": ["<option 1>", "<option 2>", "<option 3>", "<option 4>"], "correct_answer": "<option n>"}]}]"""
        prompts[getString(R.string.tipo_4)] = " $tema  $dificultad."
        prompts[getString(R.string.tipo_5)] = " $tema  $dificultad."


        // Asignar valores a tipo según el botón clickeado y mostrar descripción adicional
        btn1.setOnClickListener {
            resetButtons() // Restablece los textos y colores antes de cambiar
            tipo = getString(R.string.tipo_1) // Asigna el valor correspondiente

            // Texto principal y descripción
            val textoPrincipal = getString(R.string.tipo_1)
            val descripcion = getString(R.string.desc_tipo_1)

            // Crear SpannableString para cambiar el tamaño de la descripción
            val spannable = SpannableString(descripcion)
            spannable.setSpan(RelativeSizeSpan(0.8f), 0, spannable.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

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
            val spannable = SpannableString(descripcion)
            spannable.setSpan(RelativeSizeSpan(0.8f), 0, spannable.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

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
            val spannable = SpannableString(descripcion)
            spannable.setSpan(RelativeSizeSpan(0.8f), 0, spannable.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

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
            val spannable = SpannableString(descripcion)
            spannable.setSpan(RelativeSizeSpan(0.8f), 0, spannable.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

            btn4.text = spannable
            btn4.setBackgroundColor(colorSeleccionado) // Cambia el color del botón seleccionado
            Toast.makeText(this, "Seleccionaste: $textoPrincipal", Toast.LENGTH_SHORT).show()
        }

        btn5.setOnClickListener {
            resetButtons() // Restablece los textos y colores antes de cambiar
            tipo = getString(R.string.tipo_5) // Asigna el valor correspondiente

            // Texto principal y descripción
            val textoPrincipal = getString(R.string.tipo_5)
            val descripcion = getString(R.string.desc_tipo_5)

            // Crear SpannableString para cambiar el tamaño de la descripción
            val spannable = SpannableString(descripcion)
            spannable.setSpan(RelativeSizeSpan(0.8f), 0, spannable.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

            btn5.text = spannable
            btn5.setBackgroundColor(colorSeleccionado) // Cambia el color del botón seleccionado
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

                // Mostrar un indicador de carga (opcional)
                val progressDialog = ProgressDialog(this)
                progressDialog.setMessage("Generando lección...")
                progressDialog.setCancelable(false)
                progressDialog.show()


                // Construir el prompt en la actividad
                val prompt = prompts[tipo] ?: run {
                    Toast.makeText(this, "Prompt no encontrado para el tipo seleccionado", Toast.LENGTH_SHORT).show()
                    progressDialog.dismiss()
                    return@setOnClickListener
                }

                // Llamar al ApiService para generar el JSON
                ApiService.solicitarAPI2(prompt, this, { throwable ->
                    // Manejar error al obtener frases
                    progressDialog.dismiss()
                    Log.e("BusquedaLeccionActivity", "Error al obtener frases: ${throwable?.message}")
                    Toast.makeText(this, "Error al obtener frases de la API", Toast.LENGTH_SHORT).show()
                }) { jsonFrases ->
                    // Esta es la nueva función de callback para recibir el JSON
                    progressDialog.dismiss()
                    val nuevaLeccion = Leccion(
                        userId = userId,
                        tipo = tipo,
                        dificultad = dificultad,
                        tema = "$tema",
                        json = jsonFrases,
                        estado = false,
                        puntaje = 0
                    )

                    // Insertar la lección en la base de datos
                    leccionViewModel.insert(nuevaLeccion)
                    Toast.makeText(this, "Lección agregada", Toast.LENGTH_SHORT).show()
                    finish()
                }
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
