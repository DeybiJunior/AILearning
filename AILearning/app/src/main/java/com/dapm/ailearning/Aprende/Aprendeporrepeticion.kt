package com.dapm.ailearning.Aprende

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
import com.dapm.ailearning.Datos.Frase
import com.dapm.ailearning.R
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException

class Aprendeporrepeticion : AppCompatActivity() {

    private lateinit var frases: List<Frase>
    private var fraseIndex = 0

    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var tvFrase: TextView
    private lateinit var tvSpeechResult: TextView
    private val RECORD_AUDIO_REQUEST_CODE = 1
    private val handler = Handler(Looper.getMainLooper())
    private var lastToastTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_aprendeporrepeticion)

        tvFrase = findViewById(R.id.tvFrase)
        tvSpeechResult = findViewById(R.id.tvSpeechResult)
        val btnSiguiente: Button = findViewById(R.id.btnSiguiente)
        val btnEscucha: Button = findViewById(R.id.btnEscucha)

        // Manejo de frases
        val jsonFrases = intent.getStringExtra("frases")
        Log.d("Aprendeporrepeticion", "JSON recibido: $jsonFrases")
        val gson = Gson()
        try {
            frases = gson.fromJson(jsonFrases, Array<Frase>::class.java).toList()
            Log.d("Aprendeporrepeticion", "Frases deserializadas: ${frases.size}")
        } catch (e: JsonSyntaxException) {
            Log.e("Aprendeporrepeticion", "Error al deserializar el JSON: ${e.message}")
        }

        if (frases.isNotEmpty()) {
            tvFrase.text = frases[fraseIndex].frase
        } else {
            tvFrase.text = "No se encontraron frases."
            btnSiguiente.isEnabled = false
        }

        btnSiguiente.setOnClickListener {
            if (validarFrase(tvFrase.text.toString(), tvSpeechResult.text.toString())) {
                fraseIndex++
                if (fraseIndex < frases.size) {
                    tvFrase.text = frases[fraseIndex].frase
                    tvSpeechResult.text = "" // Limpiar el resultado del reconocimiento de voz

                } else {
                    btnSiguiente.isEnabled = false
                    tvFrase.text = "¡Has llegado al final!"
                    tvSpeechResult.text = ""

                }
            } else {
                showToast("Las frases no coinciden, por favor repite correctamente.")
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

    // Función para validar si las frases coinciden
    private fun validarFrase(fraseOriginal: String, fraseEscuchada: String): Boolean {
        val distancia = calcularDistanciaLevenshtein(fraseOriginal, fraseEscuchada)
        val longitudFraseOriginal = fraseOriginal.length

        // Calcula el porcentaje de similitud
        val porcentajeSimilitud = (1 - (distancia.toDouble() / longitudFraseOriginal)) * 100
        return porcentajeSimilitud >= 80
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
                        // Aquí podrías implementar la lógica para solicitar que el idioma se descargue.
                    }

                    else -> {
                        showToast("Error: $error")
                    }
                }
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (matches != null) {
                    tvSpeechResult.text = matches[0]
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
            checkAudioPermission() // Si no tiene permisos, los solicitamos antes de iniciar el reconocimiento.
        }
    }

    private fun showToast(message: String) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastToastTime > 2000) {
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
                // Permiso concedido
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
