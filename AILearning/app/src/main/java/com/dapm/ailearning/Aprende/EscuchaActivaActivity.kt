package com.dapm.ailearning.Aprende

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Button
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dapm.ailearning.Datos.AppDatabase
import com.dapm.ailearning.Aprende.DateQuiz.LeccionJson
import com.dapm.ailearning.R
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class EscuchaActivaActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var tvPregunta: TextView
    private lateinit var radioGroup: RadioGroup
    private lateinit var radioOpcion1: RadioButton
    private lateinit var radioOpcion2: RadioButton
    private lateinit var radioOpcion3: RadioButton
    private lateinit var radioOpcion4: RadioButton
    private lateinit var btnContinuar: Button
    private lateinit var btnEscuchar: Button
    private lateinit var tvPuntaje: TextView // TextView para mostrar el puntaje


    private lateinit var tts: TextToSpeech

    private lateinit var imageViewEstrellas: ImageView // Agregar una declaración para el ImageView de estrellas
    private lateinit var tvPuntajeFinal: TextView // Asegúrate de tener una referencia a tu TextView de puntaje final

    private var mediaPlayer: MediaPlayer? = null
    private var frases: List<LeccionJson> = emptyList()
    private var respuestaCorrecta: String? = null
    private var currentQuestionIndex = 0
    private var totalPreguntas = 0

    private var idLeccion: Int = -1

    private var puntaje = 0 // Inicializar el puntaje en cero

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_escucha_activa)

        imageViewEstrellas = findViewById(R.id.imageViewEstrellas) // Inicializa aquí
        tvPuntajeFinal = findViewById(R.id.tvPuntajeFinal) // Inicializa el TextView para el puntaje final

        // Inicializar vistas
        tvPregunta = findViewById(R.id.tvPregunta)
        radioGroup = findViewById(R.id.radioGroup)
        radioOpcion1 = findViewById(R.id.radioOpcion1)
        radioOpcion2 = findViewById(R.id.radioOpcion2)
        radioOpcion3 = findViewById(R.id.radioOpcion3)
        radioOpcion4 = findViewById(R.id.radioOpcion4)
        btnContinuar = findViewById(R.id.btnContinuar)
        btnEscuchar = findViewById(R.id.btnEscuchar)
        tvPuntaje = findViewById(R.id.tvPuntaje)

        // Inicializar Text-to-Speech
        tts = TextToSpeech(this, this)

        // Mostrar puntaje inicial (cero)
        actualizarPuntaje()

        // Obtener el idLeccion desde el intent
        idLeccion = intent.getIntExtra("idLeccion", -1)

        if (idLeccion != -1) {
            cargarLeccion(idLeccion)
        } else {
            Toast.makeText(this, "No se encontró el ID de la lección", Toast.LENGTH_SHORT).show()
        }

        // Configurar el botón de continuar
        btnContinuar.setOnClickListener {
            verificarRespuestaYContinuar()
        }

        // Configurar el botón para escuchar el texto
        btnEscuchar.setOnClickListener {
            leerTexto()
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts.setLanguage(Locale("en", "US"))

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(this, "Idioma no soportado", Toast.LENGTH_SHORT).show()
            } else {
                btnEscuchar.isEnabled = true
            }
        } else {
            Toast.makeText(this, "Inicialización de TextToSpeech fallida", Toast.LENGTH_SHORT).show()
        }
    }

    private fun leerTexto() {
        if (frases.isNotEmpty()) {
            val textToRead = frases[0].reading
            if (textToRead.isNotEmpty()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    tts.speak(textToRead, TextToSpeech.QUEUE_FLUSH, null, null)
                } else {
                    tts.speak(textToRead, TextToSpeech.QUEUE_FLUSH, null)
                }
            }
        }
    }

    private fun cargarLeccion(lessonId: Int) {
        Log.d("EscuchaActivaActivity", "Iniciando carga de lección para lessonId: $lessonId")

        CoroutineScope(Dispatchers.IO).launch {
            val leccionDao = AppDatabase.getDatabase(applicationContext).leccionDao()

            val jsonFrases = leccionDao.getJsonByLessonId(lessonId)
            Log.d("EscuchaActivaActivity", "JSON recibido: $jsonFrases")

            val gson = Gson()
            try {
                frases = gson.fromJson(jsonFrases, Array<LeccionJson>::class.java).toList()

                // Calcular el total de preguntas después de cargar las frases
                totalPreguntas = if (frases.isNotEmpty()) {
                    frases[0].quiz.size // Obtener el tamaño del quiz de la primera frase
                } else {
                    0 // Si no hay frases, el tamaño es 0
                }

                withContext(Dispatchers.Main) {
                    if (frases.isNotEmpty()) {
                        mostrarFraseYQuiz()
                    } else {
                        Toast.makeText(this@EscuchaActivaActivity, "No se encontraron frases.", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: JsonSyntaxException) {
                Log.e("EscuchaActivaActivity", "Error al deserializar el JSON: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@EscuchaActivaActivity, "Error al cargar las frases.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun mostrarFraseYQuiz() {
        val quiz = frases[0].quiz[currentQuestionIndex]
        tvPregunta.text = quiz.question

        radioOpcion1.text = quiz.options[0]
        radioOpcion2.text = quiz.options[1]
        radioOpcion3.text = quiz.options[2]
        radioOpcion4.text = quiz.options[3]

        respuestaCorrecta = quiz.correct_answer

        radioGroup.clearCheck()
    }

    private fun verificarRespuestaYContinuar() {
        val selectedOptionId = radioGroup.checkedRadioButtonId

        if (selectedOptionId == -1) {
            Toast.makeText(this, "Por favor selecciona una respuesta", Toast.LENGTH_SHORT).show()
            return
        }

        val respuestaSeleccionada = findViewById<RadioButton>(selectedOptionId).text.toString()

        if (respuestaSeleccionada == respuestaCorrecta) {
            puntaje++ // Incrementar el puntaje
            actualizarPuntaje() // Actualizar el TextView del puntaje
            Toast.makeText(this, "¡Correcto!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Respuesta incorrecta.", Toast.LENGTH_SHORT).show()
        }

        currentQuestionIndex++
        if (currentQuestionIndex < frases[0].quiz.size) {
            mostrarFraseYQuiz()
        } else {
            //Toast.makeText(this, "Has completado todas las preguntas.", Toast.LENGTH_SHORT).show()
            finalizarLeccion() // Llamar a la función para finalizar la lección
        }
    }

    private fun actualizarPuntaje() {
        tvPuntaje.text = "Puntaje: $puntaje" // Actualizar el puntaje en el TextView
    }
    private fun finalizarLeccion() {
        // Ocultar elementos de la interfaz
        btnContinuar.visibility = View.GONE
        btnEscuchar.visibility = View.GONE
        tvPregunta.visibility = View.GONE
        radioGroup.visibility = View.GONE
        tvPuntaje.visibility = View.GONE

        // Mostrar el mensaje de finalización
        val tvFraseFinal: TextView = findViewById(R.id.tvFraseFinal)
        tvFraseFinal.text = "¡Has llegado al final!"
        tvFraseFinal.visibility = View.VISIBLE
        tvFraseFinal.alpha = 0f
        tvFraseFinal.scaleX = 0.5f
        tvFraseFinal.scaleY = 0.5f

        tvFraseFinal.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(1000)
            .setInterpolator(android.view.animation.AccelerateDecelerateInterpolator())
            .start()

        // Mostrar el puntaje final
        mostrarPuntajeFinal() // Llama a la función para mostrar el puntaje final

        // Mostrar imagen de resultado
        val imagenResultado: ImageView = findViewById(R.id.imagenResultado)
        val estrellas = calcularEstrellas() // Asegúrate de que esta función esté definida
        mostrarEstrellas(estrellas) // Muestra las estrellas en función del puntaje

        if (estrellas > 0) {
            imagenResultado.setImageResource(R.drawable.aprovado)
        } else {
            imagenResultado.setImageResource(R.drawable.desaprovado)
        }
        imagenResultado.visibility = View.VISIBLE

        // Cerrar la actividad después de un tiempo
        Handler(Looper.getMainLooper()).postDelayed({
            finish()
        }, 5000)
    }

    private fun mostrarPuntajeFinal() {
        tvPuntajeFinal.visibility = View.VISIBLE
        tvPuntajeFinal.alpha = 1f // Asegúrate de que sea visible desde el principio
        tvPuntajeFinal.text = "Puntaje Final: ${puntaje}" // Mostrar el puntaje final directamente

        // Si deseas animar la aparición del texto
        tvPuntajeFinal.animate()
            .alpha(1f) // Asegúrate de que esté visible
            .setDuration(1000) // Duración de la animación de desvanecimiento
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()
        actualizarLeccion(puntaje)
    }
    private fun actualizarLeccion(puntajeFinal: Int) {
        // Crear una corrutina para verificar el estado de la lección en la base de datos
        CoroutineScope(Dispatchers.IO).launch {
            val leccionDao = AppDatabase.getDatabase(applicationContext).leccionDao() // Obtén una instancia de tu DAO

            // Verificar el estado de la lección
            val leccionActual = leccionDao.getLeccionById(idLeccion) // Supone que tienes un método para obtener la lección por ID

            if (leccionActual != null && leccionActual.estado) {
                // La lección ya está completada, no se actualiza el puntaje
                Log.d("ActualizarLeccion", "La lección ya está completada: ID = $idLeccion, no se actualizará el puntaje.")
            } else {
                val estadoFinal = true // Estado de la lección completada
                // Actualiza la lección usando la propiedad de clase
                leccionDao.updateLeccion(idLeccion, estadoFinal, puntajeFinal)

                // Log para confirmar la actualización
                Log.d("ActualizarLeccion", "Lección actualizada: ID = $idLeccion, Puntaje = $puntajeFinal, Estado = $estadoFinal")
            }
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

    private fun calcularEstrellas(): Int {
        val porcentajeCorrectas = (puntaje.toDouble() / totalPreguntas) * 100
        return when {
            porcentajeCorrectas == 100.0 -> 3 // Tres estrellas
            porcentajeCorrectas > 80.0 -> 2 // Dos estrellas
            porcentajeCorrectas >= 50 -> 1 // Una estrella
            else -> 0 // Cero estrellas
        }
    }

    private fun reproducirSonido(sonido: Int) { mediaPlayer?.release()
        mediaPlayer = MediaPlayer.create(this, sonido)
        mediaPlayer?.start()

        mediaPlayer?.setOnCompletionListener {
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    override fun onDestroy() {
        if (tts != null) {
            tts.stop()
            tts.shutdown()
        }
        super.onDestroy()
    }
}
