package com.dapm.ailearning.Inicio

import android.app.Dialog
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
import com.dapm.ailearning.Datos.Leccion
import com.dapm.ailearning.Datos.LeccionViewModel
import com.dapm.ailearning.R
import com.dapm.ailearning.Solicitudnuve.ApiService
import kotlin.properties.Delegates

class BusquedaLeccionActivity : AppCompatActivity() {

    private lateinit var leccionViewModel: LeccionViewModel

    // Colores
    private var colorNoSeleccionado by Delegates.notNull<Int>()
    val prompts = mutableMapOf<String, String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_busqueda_leccion)

        // Obtener colores desde resources
        var colorSeleccionado1 = ContextCompat.getColor(this, R.color.buttonBackground)
        var colorSeleccionado2 = ContextCompat.getColor(this, R.color.buttonBackground2)
        var colorSeleccionado3 = ContextCompat.getColor(this, R.color.buttonBackground3)
        var colorSeleccionado4 = ContextCompat.getColor(this, R.color.buttonBackground4)
        var colorSeleccionado5 = ContextCompat.getColor(this, R.color.buttonBackground5)
        var colorSeleccionado6 = ContextCompat.getColor(this, R.color.buttonBackground6)


        colorNoSeleccionado = ContextCompat.getColor(this, R.color.md_theme_inverseOnSurface)


        // Inicializar el ViewModel
        leccionViewModel = ViewModelProvider(this).get(LeccionViewModel::class.java)

        // Obtener el userId desde SharedPreferences
        val sharedPreferences = getSharedPreferences("user_data", MODE_PRIVATE)
        val userId = sharedPreferences.getString("user_id", null)
        val dificultad = sharedPreferences.getString("dificultad", "Básico") ?: "Básico" // Valor por defecto: "Básico"


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
        val btn6 = findViewById<Button>(R.id.btn6)


        // Función para restablecer textos y colores de los botones
        fun resetButtons() {
            btn1.text = getString(R.string.tipo_1)
            btn2.text = getString(R.string.tipo_2)
            btn3.text = getString(R.string.tipo_3)
            btn4.text = getString(R.string.tipo_4)
            btn5.text = getString(R.string.tipo_5)
            btn6.text = getString(R.string.tipo_6)

            btn1.setBackgroundColor(colorNoSeleccionado)
            btn2.setBackgroundColor(colorNoSeleccionado)
            btn3.setBackgroundColor(colorNoSeleccionado)
            btn4.setBackgroundColor(colorNoSeleccionado)
            btn4.setBackgroundColor(colorNoSeleccionado)
            btn5.setBackgroundColor(colorNoSeleccionado)
            btn6.setBackgroundColor(colorNoSeleccionado)

        }

        resetButtons()

        prompts[getString(R.string.tipo_1)] = "Genera un JSON con una lista de 10 frases en inglés de nivel básico $dificultad sobre $tema. La estructura debe ser: [{ \"ID\": <número>, \"frase\": \"<String>\" }, ...]"
        prompts[getString(R.string.tipo_2)] = """Generar un JSON con 1 lectura corta (máximo 50 palabras, en inglés) sobre $tema para una dificultad básica de $dificultad, todo el textto en idioma inglés. Realizar un cuestionario sobre lectura que conste de 3 preguntas de opción múltiple con 4 opciones y 1 respuesta correcta.  La estructura debe ser: [{"ID": <number>, "reading": "<String>", "quiz": [{"question": "<String>", "options": ["<option 1>", "<option 2>", "<option 3>", "<option 4>"], "correct_answer": "<option n>"}]}]"""
        prompts[getString(R.string.tipo_3)] = """Genera un JSON con 1 lectura corta (max 50 palabras) en Ingles sobre $tema de nivel básico - $dificultad. Hacer un cuestionario sobre lectura que conste de 3 preguntas de opción múltiple con 4 opciones y 1 respuesta correcta. Estructura de Ejemplo: [{"ID": <number>, "reading": "<String>", "quiz": [{"question": "<String>", "options": ["<option 1>", "<option 2>", "<option 3>", "<option 4>"], "correct_answer": "<option n>"}]}]"""
        prompts[getString(R.string.tipo_4)] = """Genera un JSON con 5 frases cortas en inglés basico-$dificultad faciles sobre $tema, cada frase con una palabra faltante en el medio (_), que sea una palabra de gramática general, como un artículo, pronombre, verbo o adjetivo, y no un nombre propio ni algo similar.. 4 opciones multiples 3 opciones incorrectas y una respuesta correcta. Estructura de Ejemplo: [{"ID": <number>, "quiz": [{"frase": "<String>", "options": ["<option 1>", "<option 2>", "<option 3>", "<option 4>"], "correct_answer": "<option n>"}]}]"""
        prompts[getString(R.string.tipo_5)] = """Generar un JSON sobre $tema para una dificultad básica de $dificultad, sobre un cuestionario en inglés que conste de 10 preguntas de opción múltiple con 4 opciones y 1 respuesta correcta.  La estructura debe ser: [{"ID": <number>, "quiz": [{"question": "<String>", "options": ["<option 1>", "<option 2>", "<option 3>", "<option 4>"], "correct_answer": "<option n>"}]}]"""
        prompts[getString(R.string.tipo_6)] = "Genera un JSON con 3 palabra en Ingles sobre $tema de nivel básico - $dificultad. con una pista la pista debe ser significado de esta en el diccionario en español. La estructura debe ser: [{ \"ID\": <número>, \"clue\": \"<String en español>\", \"oneword\": \"<String palabra en ingles>\" }]"

        val btnAgregarLeccion = findViewById<Button>(R.id.btnAgregarLeccion)
        btnAgregarLeccion.isEnabled = false // Inicialmente deshabilitado

        fun updateButtonAgregarState() {
            btnAgregarLeccion.isEnabled = tipo.isNotEmpty()
        }

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
            btn1.setBackgroundColor(colorSeleccionado1) // Cambia el color del botón seleccionado
            updateButtonAgregarState()
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
            btn2.setBackgroundColor(colorSeleccionado2) // Cambia el color del botón seleccionado
            updateButtonAgregarState()
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
            btn3.setBackgroundColor(colorSeleccionado3) // Cambia el color del botón seleccionado
            updateButtonAgregarState()
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
            btn4.setBackgroundColor(colorSeleccionado4) // Cambia el color del botón seleccionado
            updateButtonAgregarState()
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
            btn5.setBackgroundColor(colorSeleccionado5) // Cambia el color del botón seleccionado
            updateButtonAgregarState()
        }

        btn6.setOnClickListener {
            resetButtons() // Restablece los textos y colores antes de cambiar
            tipo = getString(R.string.tipo_6) // Asigna el valor correspondiente

            // Texto principal y descripción
            val textoPrincipal = getString(R.string.tipo_6)
            val descripcion = getString(R.string.desc_tipo_6)

            // Crear SpannableString para cambiar el tamaño de la descripción
            val spannable = SpannableString(descripcion)
            spannable.setSpan(RelativeSizeSpan(0.8f), 0, spannable.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

            btn6.text = spannable
            btn6.setBackgroundColor(colorSeleccionado6) // Cambia el color del botón seleccionado
            updateButtonAgregarState()
        }

        btnAgregarLeccion.setOnClickListener {
            if (userId != null) {
                // Verificar que tipo no esté vacío
                if (tipo.isEmpty()) {
                    Toast.makeText(this, "Debes seleccionar un tipo de lección", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener // Salir del onClick si tipo está vacío
                }

                // Mostrar un indicador de carga (opcional)
                val progressDialogView = layoutInflater.inflate(R.layout.custom_progress_dialog, null)
                val progressDialog = Dialog(this).apply {
                    setContentView(progressDialogView)
                    setCancelable(false)
                    window?.setBackgroundDrawableResource(android.R.color.transparent)
                }
                progressDialog.show()


                // Construir el prompt en la actividad
                val prompt = prompts[tipo] ?: run {
                    Toast.makeText(this, "Prompt no encontrado para el tipo seleccionado", Toast.LENGTH_SHORT).show()
                    progressDialog.dismiss()
                    return@setOnClickListener
                }

                // Llamar al ApiService para generar el JSON
// Llamar al ApiService para generar el JSON
                ApiService.solicitarAGemini(prompt, this, { throwable ->
                    // Manejar error al obtener frases
                    progressDialog.dismiss()
                    Log.e("BusquedaLeccionActivity", "Error al obtener frases de Gemini: ${throwable?.message}")
                    Toast.makeText(this, "Error al obtener frases de Gemini", Toast.LENGTH_SHORT).show()
                }) { jsonFrases ->

                    val jsonFrases = jsonFrases
                        .replace("```json", "", ignoreCase = true)
                        .replace("```", "")
                        .trim()

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
