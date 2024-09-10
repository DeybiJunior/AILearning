package com.dapm.ailearning

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.RecognitionListener
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

class InicioFragment : Fragment() {

    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var txtSpeechResult: TextView
    private val RECORD_AUDIO_REQUEST_CODE = 1
    private val handler = Handler(Looper.getMainLooper())
    private var lastToastTime: Long = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_inicio, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        txtSpeechResult = view.findViewById(R.id.txtSpeechResult)
        val btnStartSpeech: Button = view.findViewById(R.id.btnStartSpeech)

        // Verificar permisos
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(android.Manifest.permission.RECORD_AUDIO),
                RECORD_AUDIO_REQUEST_CODE
            )
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(requireContext())

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
        }

        speechRecognizer.setRecognitionListener(object : RecognitionListener {
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
                showToast("Error: $error")
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (matches != null) {
                    txtSpeechResult.text = matches[0]
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {}

            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        if (!SpeechRecognizer.isRecognitionAvailable(requireContext())) {
            Toast.makeText(requireContext(), "Reconocimiento de voz no disponible", Toast.LENGTH_SHORT).show()
            val intent = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)
            try {
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                e.printStackTrace()
            }
            return
        }

        btnStartSpeech.setOnClickListener {
            startVoiceRecognition(intent)
        }
    }

    private fun startVoiceRecognition(intent: Intent) {
        speechRecognizer.startListening(intent)
    }

    private fun showToast(message: String) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastToastTime > 2000) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            lastToastTime = currentTime
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == RECORD_AUDIO_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso concedido, puedes continuar
            } else {
                Toast.makeText(requireContext(), "Microphone permission is required to use speech recognition", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        speechRecognizer.destroy()
    }
}
