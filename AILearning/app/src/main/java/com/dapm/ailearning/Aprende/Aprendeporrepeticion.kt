package com.dapm.ailearning.Aprende

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.RecognitionListener
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ImageView
import com.dapm.ailearning.Datos.Frase
import com.dapm.ailearning.R
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan
import android.graphics.Typeface
import android.media.MediaPlayer
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.ProgressBar
import android.widget.RelativeLayout
import com.dapm.ailearning.Datos.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Aprendeporrepeticion : AppCompatActivity() {

    private lateinit var frases: List<Frase>
    private var Indexprogres = 0

    private var fraseIndex = 0
    private var puntajeUsuarioPorLeccion = 0
    private var totalFrases = 0
    private var textoFormateado: String = ""
    private var esPrimerClick = true // Variable para determinar si es el primer clic
    private lateinit var progressBar: ProgressBar

    private lateinit var btnEscucha: Button // Declarar btnEscucha
    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var tvFrase: TextView
    private lateinit var tvFraseFinal: TextView
    private lateinit var tilSpeechResult: TextInputLayout
    private lateinit var etSpeechResult: TextInputEditText
    private lateinit var tvPuntaje: TextView
    private lateinit var tvPuntajeFinal: TextView
    private lateinit var imageViewEstrellas: ImageView
    private lateinit var imageContacto: ImageView
    private val RECORD_AUDIO_REQUEST_CODE = 1
    private val handler = Handler(Looper.getMainLooper())
    private var lastToastTime: Long = 0
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var resultadoTextView: TextView
    private lateinit var imagenResultado: ImageView
    private lateinit var relativeLayout: RelativeLayout
    private lateinit var tvProgreso: TextView



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_aprendeporrepeticion)
        val idLeccion = intent.getIntExtra("idLeccion", -1)

        if (idLeccion != -1) {
            // Cargar las frases en un hilo separado
            cargarFrases(idLeccion)
        } else {
            // Manejar el error si no se pasó un ID válido
            Log.e("Aprendeporrepeticion", "ID de lección no válido.")
        }
        inicializarVistas()
        configurarBotones()
        checkAudioPermission()
        iniciarReconocimientoDeVoz()

        val closeButton: ImageView = findViewById(R.id.clouse)
        closeButton.setOnClickListener {
            finish()
        }
    }


    private fun inicializarVistas() {
        tvFraseFinal = findViewById(R.id.tvFraseFinal)
        tvFrase = findViewById(R.id.tvFrase)
        tilSpeechResult = findViewById(R.id.tilSpeechResult)
        etSpeechResult = findViewById(R.id.etSpeechResult)
        tvPuntaje = findViewById(R.id.tvPuntaje)
        tvPuntajeFinal = findViewById(R.id.tvPuntajeFinal)
        imageViewEstrellas = findViewById(R.id.imageViewEstrellas)
        imageContacto = findViewById(R.id.imageContacto)

        // Inicializa las nuevas variables
        resultadoTextView = findViewById(R.id.resultadoTextView) // Asegúrate de que el ID sea correcto
        imagenResultado = findViewById(R.id.imagenResultado) // Asegúrate de que el ID sea correcto
        relativeLayout = findViewById(R.id.relativeLayout) // Asegúrate de que el ID sea correcto

        btnEscucha = findViewById(R.id.btnEscucha)
        progressBar = findViewById(R.id.progressBar)
        tvProgreso = findViewById(R.id.tvProgreso)
    }


    private fun cargarFrases(lessonId: Int) {
        Log.d("Aprendeporrepeticion", "Iniciando carga de frases para lessonId: $lessonId")

        // Crear un hilo para realizar la operación de la base de datos
        CoroutineScope(Dispatchers.IO).launch {
            // Obtener el DAO de tu base de datos
            val leccionDao = AppDatabase.getDatabase(applicationContext).leccionDao()

            // Obtener el JSON de la base de datos
            val jsonFrases = leccionDao.getJsonByLessonId(lessonId)
            Log.d("Aprendeporrepeticion", "JSON recibido: $jsonFrases")

            // Deserializar el JSON en una lista de objetos Frase
            val gson = Gson()
            try {
                frases = gson.fromJson(jsonFrases, Array<Frase>::class.java).toList()
                totalFrases = frases.size
                Log.d("Aprendeporrepeticion", "Frases deserializadas: $frases")

                // Actualizar la UI en el hilo principal
                withContext(Dispatchers.Main) {
                    if (frases.isNotEmpty()) {
                        actualizarTextoFrase()
                    } else {
                        tvFrase.text = "No se encontraron frases."
                    }
                }
            } catch (e: JsonSyntaxException) {
                Log.e("Aprendeporrepeticion", "Error al deserializar el JSON: ${e.message}")
            }
        }
    }


    private fun configurarBotones() {
        val btnSiguiente: Button = findViewById(R.id.btnSiguiente)
        val btnEscucha: Button = findViewById(R.id.btnEscucha)

        btnSiguiente.setOnClickListener {
            manejarSiguiente(btnSiguiente)
        }
        btnEscucha.setOnClickListener {
            startVoiceRecognition(createSpeechIntent())
        }
    }

    private fun manejarSiguiente(btnSiguiente: Button) {
        val fraseOriginal = textoFormateado
        val fraseEscuchada = etSpeechResult.text.toString()

        Log.d("FraseOriginal", fraseOriginal)
        Log.d("FraseEscuchada", fraseEscuchada)

        // Cambia el color por defecto a verde
        val defaultColor = resources.getColor(R.color.holo_green_ligth, null)
        val redColor = resources.getColor(R.color.md_theme_error, null)

        // Si coincide
        if (validarFrase(fraseOriginal, fraseEscuchada)) {
            if (esPrimerClick) {
                mostrarResultado("EXCELENTE!\nSu pronunciamiento fue correcto", true, defaultColor, btnSiguiente)
                esPrimerClick = false // Cambiar a false después del primer clic
                puntajeUsuarioPorLeccion++
                tvPuntaje.text = "Puntaje: $puntajeUsuarioPorLeccion"
                actualizarProgreso()
                return
            } else {
                ocultarElementos(btnSiguiente) // Llama a la función ocultarElementos
                esPrimerClick = true
                fraseIndex++
            }
        } else {
            mostrarResultado("LO SENTIMOS!\nSu pronunciamiento no fue correcto", false, redColor, btnSiguiente)
            if (esPrimerClick) {
                esPrimerClick = false
                actualizarProgreso()
                return
            } else {
                ocultarElementos(btnSiguiente) // Llama a la función ocultarElementos
                esPrimerClick = true
                fraseIndex++
            }
        }

        // Actualizar la frase si aún hay más frases
        if (fraseIndex < frases.size) {
            actualizarTextoFrase()
            etSpeechResult.setText("") // Reiniciar resultado de reconocimiento
        } else {
            // Finalizar la lección sin mostrar el resultado
            finalizarLeccion(btnSiguiente)
        }
    }

    private fun mostrarResultado(mensaje: String, esCorrecto: Boolean, color: Int, btnSiguiente: Button) {

        btnEscucha.visibility = View.GONE
        val relativeLayout: RelativeLayout = findViewById(R.id.relativeLayout) // Asegúrate de que el ID sea correcto
        relativeLayout.visibility = View.GONE

        val resultadoTextView: TextView = findViewById(R.id.resultadoTextView) // Asegúrate de tener un TextView en tu layout
        resultadoTextView.text = mensaje

        val imagenResultado: ImageView = findViewById(R.id.imagenResultado) // Asegúrate de tener una ImageView en tu layout
        imagenResultado.setImageResource(if (esCorrecto) R.drawable.excelente else R.drawable.losentimos) // Usa tus imágenes

        resultadoTextView.visibility = View.VISIBLE
        imagenResultado.visibility = View.VISIBLE

        resultadoTextView.text = mensaje
        resultadoTextView.setTextColor(color) // Cambia el color del texto
        resultadoTextView.visibility = View.VISIBLE // Asegúrate de que el TextView sea visible


        // Cambia el color del botón según el resultado
        if (!esCorrecto) {
            btnSiguiente.setBackgroundColor(resources.getColor(R.color.md_theme_error, null))
        } else {
            btnSiguiente.setBackgroundColor(resources.getColor(R.color.holo_green_ligth, null)) // Color por defecto si es correcto
        }
    }





    private fun actualizarProgreso() {
        Indexprogres++
        // Calcula el progreso actual basado en el índice de la frase y el total de frases
        val progreso = ((Indexprogres) * 100) / 4

        // Crear un ObjectAnimator para animar el progreso del ProgressBar
        val animator = ObjectAnimator.ofInt(progressBar, "progress", progressBar.progress, progreso)
        animator.duration = 500 // Duración de la animación (medio segundo)
        animator.interpolator = DecelerateInterpolator() // Para un efecto suave
        animator.start() // Inicia la animación
    }






    fun ocultarElementos(btnSiguiente: Button) {
        resultadoTextView.visibility = View.GONE // Oculta el TextView
        btnSiguiente.setBackgroundColor(resources.getColor(R.color.holo_green_ligth, null)) // Vuelve al color por defecto
        resultadoTextView.visibility = View.GONE
        imagenResultado.visibility = View.GONE
        relativeLayout.visibility = View.VISIBLE
        btnEscucha.visibility =View.VISIBLE
    }


    private fun finalizarLeccion(btnSiguiente: Button) {
        if (::btnEscucha.isInitialized) {
            btnEscucha.visibility = View.GONE
        }
        btnSiguiente.visibility = View.GONE
        tvFraseFinal.visibility = View.VISIBLE
        tvProgreso.visibility = View.GONE
        progressBar.visibility = View.GONE
        tvFraseFinal.alpha = 0f
        tvFraseFinal.scaleX = 0.5f
        tvFraseFinal.scaleY = 0.5f

        tvFraseFinal.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(1000)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()

        val imagenResultado: ImageView = findViewById(R.id.imagenResultado) // Asegúrate de tener una ImageView en tu layout
        val estrellas = calcularEstrellas() // Llama a calcularEstrellas

        if (estrellas > 0) {
            imagenResultado.setImageResource(R.drawable.aprovado)
        } else {
            imagenResultado.setImageResource(R.drawable.desaprovado)
        }

        imagenResultado.visibility = View.VISIBLE

        imageContacto.visibility = View.GONE
        tvFrase.visibility = View.GONE
        etSpeechResult.setText("")
        tvPuntaje.visibility = View.GONE
        btnEscucha.visibility = View.GONE
        tilSpeechResult.visibility = View.GONE


        mostrarPuntajeFinal()
        Handler(Looper.getMainLooper()).postDelayed({
            finish()
        }, 5000)
    }



    private fun iniciarReconocimientoDeVoz() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizer.setRecognitionListener(createRecognitionListener())
    }

    // Función para actualizar el texto con la frase dinámica
    private fun actualizarTextoFrase() {
        val textoConstante = "Lee y repite:\nNo tengas miedo a equivocarte, practicar es lo más importante.\n"
        val textoFrase = frases[fraseIndex].frase
        textoFormateado = "\n\" $textoFrase \""

        // Crear un SpannableString con el texto completo
        val spannableString = SpannableString(textoConstante + textoFormateado)

        // Aplicar estilo cursiva solo a la frase entre comillas
        spannableString.setSpan(
            StyleSpan(Typeface.ITALIC),
            textoConstante.length, // Empieza a aplicar el estilo justo después del texto constante
            (textoConstante + textoFormateado).length, // Hasta el final de la frase formateada
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // Asignar el SpannableString al TextView
        tvFrase.text = spannableString
    }

    private fun mostrarPuntajeFinal() {
        // Inicializa el TextView y establece la visibilidad en VISIBLE
        tvPuntajeFinal.visibility = View.VISIBLE
        tvPuntajeFinal.alpha = 0f // Comienza invisible
        tvPuntajeFinal.text = "Puntaje Final: 0" // Comienza en 0

        val startValue = 0
        val endValue = puntajeUsuarioPorLeccion // Este es el puntaje final

        // Crea un ValueAnimator para animar el puntaje
        val animator = ValueAnimator.ofInt(startValue, endValue)
        animator.duration = 2000 // Duración de 2 segundos
        animator.addUpdateListener { animation ->
            val animatedValue = animation.animatedValue as Int
            tvPuntajeFinal.text = "Puntaje Final: $animatedValue"
        }
        animator.start()

        // Animación de desvanecimiento para el puntaje
        tvPuntajeFinal.animate()
            .alpha(1f)
            .setDuration(1000) // Duración de la animación de desvanecimiento
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()

        // Calcular y mostrar estrellas
        val estrellas = calcularEstrellas()
        mostrarEstrellas(estrellas)
    }



    private fun calcularEstrellas(): Int {
        val porcentajeCorrectas = (puntajeUsuarioPorLeccion.toDouble() / totalFrases) * 100
        return when {
            porcentajeCorrectas == 100.0 -> 3 // Tres estrellas
            porcentajeCorrectas > 80.0 -> 2 // Dos estrellas
            porcentajeCorrectas >= 50 -> 1 // Una estrella
            else -> 0 // Cero estrellas
        }
    }

    private fun mostrarEstrellas(estrellas: Int) {
        // Cambiar la imagen según el puntaje
        when (estrellas) {
            3 -> {
                imageViewEstrellas.setImageResource(R.drawable.estrella3)
                reproducirSonido(R.raw.felicitaciones)
            }
            2 -> {
                imageViewEstrellas.setImageResource(R.drawable.estrella2)
            }
            1 -> {
                imageViewEstrellas.setImageResource(R.drawable.estrella1)
            }
            0 -> {
                imageViewEstrellas.setImageResource(R.drawable.vuelveintentar)
                reproducirSonido(R.raw.felicitaciones)
            }
        }

        imageViewEstrellas.alpha = 0f
        imageViewEstrellas.visibility = View.VISIBLE
        imageViewEstrellas.animate()
            .alpha(1f) // Animar a alpha 1 (visible)
            .setDuration(500) // Duración de la animación
            .start() // Iniciar la animación
    }

    private fun reproducirSonido(sonido: Int) { mediaPlayer?.release()
        mediaPlayer = MediaPlayer.create(this, sonido)
        mediaPlayer?.start()

        mediaPlayer?.setOnCompletionListener {
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }


    // Función para validar si las frases coinciden
    private fun validarFrase(fraseOriginal: String, fraseEscuchada: String): Boolean {
        val fraseOriginalNormalizada = normalizarTexto(fraseOriginal)
        val fraseEscuchadaNormalizada = normalizarTexto(fraseEscuchada)
        Log.d("ValidarFrase", "Frase Original Normalizada: $fraseOriginalNormalizada")
        Log.d("ValidarFrase", "Frase Escuchada Normalizada: $fraseEscuchadaNormalizada")


        val distancia = calcularDistanciaLevenshtein(fraseOriginalNormalizada, fraseEscuchadaNormalizada)
        val longitudFraseOriginal = fraseOriginalNormalizada.length

        // Calcula el porcentaje de similitud
        val porcentajeSimilitud = (1 - (distancia.toDouble() / longitudFraseOriginal)) * 100
        return porcentajeSimilitud >= 70
    }

    // Función para normalizar el texto
// Función para normalizar el texto
    private fun normalizarTexto(texto: String): String {
        // Elimina puntuaciones y convierte a minúsculas
        return texto
            .replace(Regex("[^a-zA-Z0-9áéíóúñü ]"), "") // Quita todos los símbolos y caracteres no deseados
            .trim() // Elimina espacios al inicio y al final
            .toLowerCase() // Convierte a minúsculas
    }



    // Función para calcular la distancia de Levenshtein
    private fun calcularDistanciaLevenshtein(str1: String, str2: String): Int {
        val dp = Array(str1.length + 1) { IntArray(str2.length + 1) }

        for (i in 0..str1.length) {
            for (j in 0..str2.length) {
                when {
                    i == 0 -> dp[i][j] = j // Deletions
                    j == 0 -> dp[i][j] = i // Insertions
                    str1[i - 1] == str2[j - 1] -> dp[i][j] = dp[i - 1][j - 1] // No change
                    else -> dp[i][j] = 1 + minOf(
                        dp[i - 1][j],
                        dp[i][j - 1],
                        dp[i - 1][j - 1]
                    ) // Deletion, insertion, substitution
                }
            }
        }
        return dp[str1.length][str2.length]
    }

    private fun checkAudioPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.RECORD_AUDIO),
                RECORD_AUDIO_REQUEST_CODE
            )
        }
    }

    private fun createSpeechIntent(): Intent {
        return Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
        }
    }

    private fun createRecognitionListener(): RecognitionListener {
        return object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                showToast("Listo para escuchar")
            }

            override fun onBeginningOfSpeech() {
                showToast("Escuchando...")
            }

            override fun onRmsChanged(rmsdB: Float) {}

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                showToast("Procesando...")
            }
            override fun onError(error: Int) {
                when (error) {
                    SpeechRecognizer.ERROR_LANGUAGE_UNAVAILABLE -> {
                        showToast("Idioma no disponible. Intente descargar el idioma.")

                        // Crear un Intent para abrir la configuración de reconocimiento de voz
                        val intent = Intent(Intent.ACTION_MAIN).apply {
                            action = "com.android.settings.TTS_SETTINGS"
                            addCategory(Intent.CATEGORY_DEFAULT)
                        }

                        // Verificar si hay una actividad que pueda manejar este Intent
                        if (intent.resolveActivity(packageManager) != null) {
                            startActivity(intent)
                        } else {
                            showToast("No se puede abrir la configuración de idiomas.")
                        }
                    }

                    else -> {
                        showToast("Error: $error")
                    }
                }
            }


            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (matches != null) {
                    etSpeechResult.setText(matches[0])
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {}

            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
    }

    private fun startVoiceRecognition(intent: Intent) {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO)
            == PackageManager.PERMISSION_GRANTED
        ) {
            speechRecognizer.startListening(intent)
        } else {
            checkAudioPermission()
        }
    }

    private fun showToast(message: String) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastToastTime > 1000) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            lastToastTime = currentTime
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        speechRecognizer.destroy()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == RECORD_AUDIO_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val intent = createSpeechIntent()
                startVoiceRecognition(intent)
            } else {
                Toast.makeText(
                    this,
                    "Se requiere permiso para usar el micrófono",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
