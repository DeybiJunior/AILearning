package com.dapm.ailearning.Aprende

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

class Aprendeporrepeticion : AppCompatActivity() {

    private lateinit var frases: List<Frase>
    private var fraseIndex = 0
    private var puntajeUsuarioPorLeccion = 0
    private var totalFrases = 0
    private var textoFormateado: String = ""

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_aprendeporrepeticion)

        // Inicializa tus vistas aquí
        tvFraseFinal = findViewById(R.id.tvFraseFinal)
        tvFrase = findViewById(R.id.tvFrase)
        tilSpeechResult = findViewById(R.id.tilSpeechResult)
        etSpeechResult = findViewById(R.id.etSpeechResult)
        tvPuntaje = findViewById(R.id.tvPuntaje)
        tvPuntajeFinal = findViewById(R.id.tvPuntajeFinal)
        imageViewEstrellas = findViewById(R.id.imageViewEstrellas)
        imageContacto = findViewById(R.id.imageContacto)
        val btnSiguiente: Button = findViewById(R.id.btnSiguiente)
        val btnEscucha: Button = findViewById(R.id.btnEscucha)

        // Manejo de frases
        val jsonFrases = intent.getStringExtra("frases")
        val gson = Gson()
        try {
            frases = gson.fromJson(jsonFrases, Array<Frase>::class.java).toList()
            totalFrases = frases.size // Guardar total de frases
        } catch (e: JsonSyntaxException) {
            Log.e("Aprendeporrepeticion", "Error al deserializar el JSON: ${e.message}")
        }

        if (frases.isNotEmpty()) {
            // Llamar la función para actualizar el texto con el formato correcto
            actualizarTextoFrase()
        } else {
            tvFrase.text = "No se encontraron frases."
            btnSiguiente.isEnabled = false
        }

        btnSiguiente.setOnClickListener {
            // Verifica si la frase escuchada es válida
            val fraseOriginal = textoFormateado
            val fraseEscuchada = etSpeechResult.text.toString()

            Log.d("FraseOriginal", fraseOriginal)
            Log.d("FraseEscuchada", fraseEscuchada)
            // Siempre avanza al siguiente índice
            fraseIndex++

            if (validarFrase(fraseOriginal, fraseEscuchada)) {
                // Si es válida, sumar el puntaje
                puntajeUsuarioPorLeccion++
                // Actualizar el TextView de puntaje
                tvPuntaje.text = "Puntaje: $puntajeUsuarioPorLeccion"
            } else {
                showToast("Las frases no coinciden")
            }

            // Verifica si hay más frases
            if (fraseIndex < frases.size) {
                actualizarTextoFrase() // Actualizar la frase con el nuevo índice
                etSpeechResult.setText("") // Reiniciar resultado de reconocimiento aquí
            } else {
                btnSiguiente.isEnabled = false // Deshabilitar botón si se acabaron las frases
                tvFraseFinal.visibility = View.VISIBLE
                    tvFraseFinal.alpha = 0f // Comienza invisible
                    tvFraseFinal.scaleX = 0.5f // Escala inicial
                    tvFraseFinal.scaleY = 0.5f // Escala inicial

                    tvFraseFinal.animate()
                        .alpha(1f)
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(1000) // Duración de la animación
                        .setInterpolator(AccelerateDecelerateInterpolator()) // Interpolador para suavizar la animación
                        .start()
                imageContacto.visibility = View.GONE
                tvFrase.visibility = View.GONE
                etSpeechResult.setText("") // Reiniciar resultado de reconocimiento
                tvPuntaje.visibility = View.GONE
                // Ocultar el botón de micrófono
                btnEscucha.visibility = View.GONE
                tilSpeechResult.visibility = View.GONE
                // Mostrar el puntaje final
                mostrarPuntajeFinal()
            }
        }

        // Manejo de reconocimiento de voz
        checkAudioPermission()

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        val speechIntent = createSpeechIntent()
        speechRecognizer.setRecognitionListener(createRecognitionListener())

        btnEscucha.setOnClickListener {
            startVoiceRecognition(speechIntent)
        }
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
                imageViewEstrellas.setImageResource(R.drawable.estrella1)
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
