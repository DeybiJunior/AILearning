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
        val dificultad = sharedPreferences.getString("dificultad", "Básico") ?: "Básico"


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


        val (contextoNivel, cantidadElementos) = when (dificultad.lowercase()) {
            "básico" -> Pair(
                "nivel muy básico para niños de 4-8 años, vocabulario extremadamente simple, palabras de uso diario",
                mapOf("frases" to 3, "lecturas" to 2, "quiz_preguntas" to 5, "palabras" to 2)
            )
            "intermedio" -> Pair(
                "nivel intermedio para estudiantes de 9-12 años, vocabulario moderado, estructuras gramaticales básicas",
                mapOf("frases" to 5, "lecturas" to 3, "quiz_preguntas" to 8, "palabras" to 3)
            )
            "avanzado" -> Pair(
                "nivel avanzado para estudiantes de 13+ años, vocabulario amplio, estructuras gramaticales complejas",
                mapOf("frases" to 7, "lecturas" to 5, "quiz_preguntas" to 10, "palabras" to 6)
            )
            else -> Pair(
                "nivel básico para principiantes, vocabulario fundamental",
                mapOf("frases" to 5, "lecturas" to 3, "quiz_preguntas" to 7, "palabras" to 3)
            )
        }

        // PROMPT TIPO 1 - FRASES SIMPLES
                prompts[getString(R.string.tipo_1)] = """
        Eres un experto en enseñanza de inglés. Genera un JSON con frases en inglés sobre $tema.
        
        Nivel de dificultad: $contextoNivel
        Cantidad de frases: ${cantidadElementos["frases"]}
        
        Requisitos específicos:
        - Frases cortas apropiadas para el nivel
        - Vocabulario adecuado para la edad y nivel
        - Gramática simple y clara
        - Temas familiares y cotidianos
        - Usar tiempos verbales básicos
        - Evitar jerga o modismos complejos
        
        Estructura JSON requerida:
        [{"ID": <número>, "frase": "<frase en inglés>"}, {"ID": <número>, "frase": "<frase en inglés>"}, ...]
        
        Responde ÚNICAMENTE con el JSON válido, sin texto adicional.
        """

        // PROMPT TIPO 2 - LECTURA CON COMPRENSIÓN
                prompts[getString(R.string.tipo_2)] = """
        Eres un creador de contenido educativo para aprendizaje de inglés. Crea una lectura corta sobre $tema.
        
        Nivel de dificultad: $contextoNivel
        Cantidad de preguntas: ${cantidadElementos["lecturas"]}
        
        Requisitos para la lectura:
        - Longitud: 40-60 palabras
        - Vocabulario apropiado para el nivel especificado
        - Oraciones simples y claras
        - Tema interesante y relevante
        - Uso de gramática adecuada al nivel
        - Evitar palabras técnicas innecesarias
        
        Requisitos para el quiz:
        - Preguntas de comprensión basadas en el texto
        - Respuestas encontrables directamente en la lectura
        - Opciones claras y diferentes
        - Una respuesta correcta obvia
        - Distractores plausibles pero incorrectos
        
        Estructura JSON requerida:
        [{"ID": <número>, "reading": "<texto de la lectura>", "quiz": [{"question": "<pregunta>", "options": ["<opción>", "<opción>", "<opción>", "<opción>"], "correct_answer": "<respuesta correcta>"}]}]
        
        Responde ÚNICAMENTE con el JSON válido, sin texto adicional.
        """

        // PROMPT TIPO 3 - LECTURA CON COMPRENSIÓN (VERSIÓN ALTERNATIVA)
                prompts[getString(R.string.tipo_3)] = """
        Eres un especialista en material didáctico para inglés. Crea una lectura educativa sobre $tema.
        
        Nivel de dificultad: $contextoNivel
        Cantidad de preguntas: ${cantidadElementos["lecturas"]}
        
        Especificaciones para la lectura:
        - Longitud: 40-80 palabras
        - Vocabulario adaptado al nivel especificado
        - Estructura narrativa simple
        - Información clara y factual
        - Uso de conectores apropiados al nivel
        - Evitar abstracciones complejas
        
        Especificaciones para las preguntas:
        - Preguntas sobre información explícita del texto
        - Evaluar comprensión literal apropiada al nivel
        - Opciones de respuesta balanceadas
        - Distractores creíbles pero claramente incorrectos
        - Preguntas variadas (quién, qué, dónde, cuándo, por qué)
        
        Estructura JSON requerida:
        [{"ID": <número>, "reading": "<texto de la lectura>", "quiz": [{"question": "<pregunta>", "options": ["<opción>", "<opción>", "<opción>", "<opción>"], "correct_answer": "<respuesta correcta>"}]}]
        
        Responde ÚNICAMENTE con el JSON válido, sin texto adicional.
        """

        // PROMPT TIPO 4 - COMPLETAR FRASES
                prompts[getString(R.string.tipo_4)] = """
        Eres un experto en gramática inglesa para principiantes. Crea ejercicios de completar frases sobre $tema.
        
        Nivel de dificultad: $contextoNivel
        Cantidad de ejercicios: ${cantidadElementos["quiz_preguntas"]}
        
        Requisitos para las frases:
        - Frases cortas y simples apropiadas al nivel
        - Una sola palabra faltante marcada con "_"
        - Palabra faltante debe ser gramática básica: artículos, verbos auxiliares, preposiciones básicas, pronombres
        - Contexto claro que indique la respuesta correcta
        - Vocabulario familiar y apropiado al nivel
        - Evitar nombres propios o términos técnicos
        
        Requisitos para las opciones:
        - 4 opciones por pregunta
        - 1 respuesta claramente correcta
        - 3 distractores plausibles pero incorrectos
        - Opciones de la misma categoría gramatical
        - Evitar opciones obviamente incorrectas
        
        Estructura JSON requerida:
        [{"ID": 1, "quiz": [{"frase": "<frase con _>", "options": ["<opción>", "<opción>", "<opción>", "<opción>"], "correct_answer": "<respuesta correcta>"}]}]
        
        REGLAS CRÍTICAS:
        - Generar UN SOLO objeto con ID: 1
        - Crear la cantidad de ejercicios especificada en el array "quiz"
        - Cada frase debe tener UNA palabra faltante con "_"
        - Responder SOLO con JSON válido, sin texto adicional
        """

        // PROMPT TIPO 5 - QUIZ GENERAL
                prompts[getString(R.string.tipo_5)] = """
        Eres un creador de evaluaciones de inglés. Diseña un quiz completo sobre $tema.
        
        Nivel de dificultad: $contextoNivel
        Cantidad de preguntas: ${cantidadElementos["quiz_preguntas"]}
        
        Requisitos para las preguntas:
        - Preguntas variadas sobre el tema
        - Preguntas claras y directas
        - Vocabulario apropiado para el nivel
        - Mezclar tipos: vocabulario, gramática, comprensión
        - Evitar preguntas demasiado específicas o técnicas
        - Incluir contexto suficiente para responder
        
        Requisitos para las opciones:
        - 4 opciones balanceadas por pregunta
        - 1 respuesta correcta obvia para el nivel
        - 3 distractores creíbles pero incorrectos
        - Opciones de longitud similar
        - Evitar opciones como "todas las anteriores" o "ninguna"
        
        Estructura JSON requerida:
        [{"ID": <número>, "quiz": [{"question": "<pregunta>", "options": ["<opción>", "<opción>", "<opción>", "<opción>"], "correct_answer": "<respuesta correcta>"}]}]
        
        REGLAS CRÍTICAS:
        - Generar UN SOLO objeto con ID: 1
        - Crear la cantidad de preguntas especificada en el array "quiz"
        - Preguntas progresivas en dificultad dentro del nivel
        - Responder SOLO con JSON válido, sin texto adicional
        """

        // PROMPT TIPO 6 - PALABRAS CON PISTAS
                prompts[getString(R.string.tipo_6)] = """
        Eres un especialista en vocabulario inglés para principiantes. Crea ejercicios de palabras con pistas sobre $tema.
        
        Nivel de dificultad: $contextoNivel
        Cantidad de palabras: ${cantidadElementos["palabras"]}
        
        Requisitos para las palabras:
        - Palabras en inglés relacionadas con el tema
        - Palabras básicas y comunes del vocabulario
        - Evitar palabras técnicas o complejas
        - Palabras que los estudiantes del nivel puedan usar
        - Preferir sustantivos, verbos y adjetivos básicos
        
        Requisitos para las pistas:
        - Definiciones claras en español
        - Explicaciones simples y directas
        - Incluir contexto de uso cuando sea útil
        - Evitar definiciones circulares
        - Usar vocabulario español accesible
        
        Estructura JSON requerida:
        [{"ID": <número>, "clue": "<pista en español>", "oneword": "<palabra en inglés>"}, {"ID": <número>, "clue": "<pista en español>", "oneword": "<palabra en inglés>"}, ...]
        
        REGLAS CRÍTICAS:
        - Generar la cantidad especificada de objetos con IDs consecutivos
        - Pistas en español, palabras en inglés
        - Relación directa entre pista y palabra
        - Responder SOLO con JSON válido, sin texto adicional
        """

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
