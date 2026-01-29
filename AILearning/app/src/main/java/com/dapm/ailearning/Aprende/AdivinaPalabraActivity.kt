package com.dapm.ailearning.Aprende

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.dapm.ailearning.Aprende.Adivina.AdivinaJson
import com.dapm.ailearning.Datos.AppDatabase
import com.dapm.ailearning.Datos.LeccionDao
import com.dapm.ailearning.R
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AdivinaPalabraActivity : AppCompatActivity() {
    private lateinit var leccionDao: LeccionDao
    private lateinit var clueTextView: TextView
    private lateinit var guessEditText: EditText
    private lateinit var submitGuessButton: Button
    private lateinit var feedbackTextView: TextView
    private lateinit var btSiguiente: Button
    private var totalPreguntas = 0
    private lateinit var letterButtonsLayout: LinearLayout
    private var failedAttempts = 10

    private lateinit var progressBar: ProgressBar // Variable para el ProgressBar
    private var indexProgres = 0 // Cambié el nombre de Indexprogres a indexProgres

    private var frases: List<AdivinaJson> = listOf()
    private var currentQuestionIndex = 0

    private var idLeccion: Int = -1

    private lateinit var tvPuntajeFinal: TextView // Asegúrate de tener esta variable para el puntaje final
    private lateinit var imageViewEstrellas: ImageView
    private lateinit var imagenResultado: ImageView
    private var score: Int = 0 // Variable para puntaje
    private lateinit var tvPuntaje: TextView

    private lateinit var tvProgreso: TextView
    private lateinit var clouse: ImageView
    private lateinit var pista: TextView
    private var startTime: Long = 0
    private var endTime: Long = 0

    private var resp = ""

    // Nuevo: Container para los intentos
    private lateinit var attemptsContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_adivina_palabra)

        clueTextView = findViewById(R.id.clueTextView)
        guessEditText = findViewById(R.id.guessEditText)
        submitGuessButton = findViewById(R.id.submitGuessButton)
        feedbackTextView = findViewById(R.id.feedbackTextView)
        progressBar = findViewById(R.id.progressBar)
        btSiguiente = findViewById(R.id.btnSiguiente)
        letterButtonsLayout = findViewById(R.id.letterButtonsLayout)
        tvProgreso = findViewById(R.id.tvProgreso)
        clouse = findViewById(R.id.clouse)
        pista = findViewById(R.id.pista)

        tvPuntaje = findViewById(R.id.tvPuntaje)
        tvPuntajeFinal = findViewById(R.id.tvPuntajeFinal) // Inicializa el TextView para el puntaje final
        imageViewEstrellas = findViewById(R.id.imageViewEstrellas)
        imagenResultado = findViewById(R.id.imagenResultado)

        // Nuevo: Inicializar container para los intentos
        attemptsContainer = findViewById(R.id.attemptsContainer)

        initializeDatabase()

        idLeccion = intent.getIntExtra("idLeccion", -1)

        // Reinicio de Respuestas seleccionadas
        CoroutineScope(Dispatchers.IO).launch {
            leccionDao.updateRespuestasSeleccionadas(idLeccion, "")
        }

        startTime = System.currentTimeMillis()
        Log.d("AdivinaPalabraActivity", "startTime initialized: $startTime")

        if (idLeccion != -1) {
            cargarLeccion(idLeccion)
        } else {
            Toast.makeText(this, "No se encontró el ID de la lección", Toast.LENGTH_SHORT).show()
        }

        submitGuessButton.setOnClickListener {
            checkGuess()
        }

        btSiguiente.setOnClickListener {
            resp = guessEditText.text.toString()
            Log.d("DEBUG_RESP", "Valor de resp: $resp")
            // Guardar la respuesta seleccionada para la pregunta actual
            if (resp == "") {
                CoroutineScope(Dispatchers.Main).launch {
                    leccionDao.agregarRespuestasSeleccionadas(idLeccion, "sin respuesta")
                }
            } else {
                CoroutineScope(Dispatchers.Main).launch {
                    leccionDao.agregarRespuestasSeleccionadas(idLeccion, resp)
                }
            }
            nextQuestion()
        }

        // Dentro de onCreate en tu actividad
        val clouseImageView: ImageView = findViewById(R.id.clouse)
        clouseImageView.setOnClickListener {
            finish() // Cierra la actividad actual
        }
    }

    private fun cargarLeccion(lessonId: Int) {
        Log.d("AdivinaPalabraActivity", "Iniciando carga de lección para lessonId: $lessonId")

        CoroutineScope(Dispatchers.IO).launch {
            val leccionDao = AppDatabase.getDatabase(applicationContext).leccionDao()

            val jsonFrases = leccionDao.getJsonByLessonId(lessonId)
            Log.d("AdivinaPalabraActivity", "JSON recibido: $jsonFrases")

            val gson = Gson()
            try {
                frases = gson.fromJson(jsonFrases, Array<AdivinaJson>::class.java).toList()
                totalPreguntas = frases.size

                withContext(Dispatchers.Main) {
                    if (frases.isNotEmpty()) {
                        clueTextView.text = frases[currentQuestionIndex].clue
                        initializeLetterButtons(frases[currentQuestionIndex].oneword.length)
                    } else {
                        Toast.makeText(this@AdivinaPalabraActivity, "No se encontraron frases.", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: JsonSyntaxException) {
                Log.e("AdivinaPalabraActivity", "Error al deserializar el JSON: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AdivinaPalabraActivity, "Error al cargar las frases.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun initializeDatabase() {
        val db = AppDatabase.getDatabase(applicationContext) // Usa el método getDatabase
        leccionDao = db.leccionDao() // Inicializa leccionDao
    }

    private fun initializeLetterButtons(length: Int) {
        // Limpia los botones anteriores
        letterButtonsLayout.removeAllViews()

        for (i in 0 until length) {
            val button = Button(this).apply {
                text = "?" // Inicializa el texto del botón, puedes cambiarlo más tarde
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                // Aquí puedes agregar más propiedades al botón
            }
            letterButtonsLayout.addView(button)
        }
    }

    private fun checkGuess() {
        val userGuess = guessEditText.text.toString().trim()
        val correctAnswer = frases[currentQuestionIndex].oneword

        // Validar que el usuario ingresó algo
        if (userGuess.isEmpty()) {
            Toast.makeText(this, "Por favor ingresa una palabra", Toast.LENGTH_SHORT).show()
            return
        }

        // Asegúrate de que la longitud de userGuess sea igual a la longitud de correctAnswer
        if (userGuess.length != correctAnswer.length) {
            Toast.makeText(this, "La palabra debe tener ${correctAnswer.length} letras.", Toast.LENGTH_SHORT).show()
            return
        }

        // Crear un nuevo intento visual
        createAttemptView(userGuess, correctAnswer)

        if (userGuess.equals(correctAnswer, ignoreCase = true)) {
            feedbackTextView.text = "¡Correcto!"
            guessEditText.isEnabled = false
            submitGuessButton.isEnabled = false

            score++
            tvPuntaje.text = "Puntaje: $score"

            return
        } else {
            failedAttempts--

            if (failedAttempts <= 0) {
                guessEditText.isEnabled = false
                submitGuessButton.isEnabled = false
                feedbackTextView.text = "Has agotado tus intentos. La palabra era \"$correctAnswer\""
                return
            } else {
                feedbackTextView.text = "Intenta de nuevo, te quedan $failedAttempts intentos."
            }
        }

        // Limpiar el campo de entrada para el siguiente intento
        guessEditText.text.clear()
    }

    private fun createAttemptView(userGuess: String, correctAnswer: String) {
        // Crear un LinearLayout horizontal para este intento
        val attemptLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 8, 0, 8)
            }
        }

        // Crear botones para cada letra del intento
        for (i in correctAnswer.indices) {
            val letterButton = Button(this).apply {
                text = userGuess.getOrNull(i)?.toString()?.uppercase() ?: ""
                layoutParams = LinearLayout.LayoutParams(0, 120, 1f).apply {
                    setMargins(4, 0, 4, 0)
                }
                textSize = 18f
                typeface = Typeface.DEFAULT_BOLD

                // Determinar el color basado en la lógica de Wordle
                val backgroundColor = when {
                    i < userGuess.length && userGuess[i].equals(correctAnswer[i], ignoreCase = true) -> {
                        // Letra correcta en posición correcta (Verde)
                        ContextCompat.getColor(this@AdivinaPalabraActivity, R.color.basicoColor)
                    }
                    i < userGuess.length && correctAnswer.contains(userGuess[i], ignoreCase = true) -> {
                        // Letra correcta en posición incorrecta (Amarillo)
                        ContextCompat.getColor(this@AdivinaPalabraActivity, R.color.intermedioColor)
                    }
                    else -> {
                        // Letra incorrecta (Gris)
                        Color.GRAY
                    }
                }

                setBackgroundColor(backgroundColor)
                setTextColor(Color.WHITE)
            }
            attemptLayout.addView(letterButton)
        }

        // Agregar el intento al container
        attemptsContainer.addView(attemptLayout)

        // Hacer scroll hacia abajo para mostrar el último intento
        val scrollView = findViewById<ScrollView>(R.id.scrollView)
        scrollView?.post {
            scrollView.fullScroll(ScrollView.FOCUS_DOWN)
        }
    }

    private fun nextQuestion() {
        if (currentQuestionIndex < frases.size - 1) {
            currentQuestionIndex++

            // Limpiar todos los intentos anteriores
            attemptsContainer.removeAllViews()

            // Restablecer la interfaz para la nueva pregunta
            clueTextView.text = frases[currentQuestionIndex].clue
            guessEditText.text.clear()
            guessEditText.isEnabled = true
            submitGuessButton.isEnabled = true
            failedAttempts = 10
            feedbackTextView.text = ""

            actualizarProgreso(totalPreguntas)
            initializeLetterButtons(frases[currentQuestionIndex].oneword.length)

        } else {
            // Ocultar elementos del juego
            clueTextView.visibility = View.GONE
            guessEditText.visibility = View.GONE
            submitGuessButton.visibility = View.GONE
            feedbackTextView.visibility = View.GONE
            progressBar.visibility = View.GONE
            btSiguiente.visibility = View.GONE
            letterButtonsLayout.visibility = View.GONE
            tvProgreso.visibility = View.GONE
            tvPuntaje.visibility = View.GONE
            clouse.visibility = View.GONE
            pista.visibility = View.GONE
            attemptsContainer.visibility = View.GONE

            mostrarPuntajeFinal()
            actualizarLeccion(score)
        }
    }

    private fun actualizarLeccion(puntajeFinal: Int) {
        // Log to confirm startTime
        Log.d("ActualizarLeccion", "startTime at call: $startTime")

        // Determinar la dificultad basada en la cantidad de palabras
        val dificultadEjercicio = when (totalPreguntas) {
            2 -> "BÁSICO"
            3 -> if (Math.random() > 0.5) "INTERMEDIO" else "DEFAULT" // Ambos tienen 3 palabras
            6 -> "AVANZADO"
            else -> "DEFAULT"
        }

        // Calcular el puntaje sobre 10 basado en la proporción de respuestas correctas
        val puntajeEnvio = if (totalPreguntas > 0) {
            ((puntajeFinal.toFloat() / totalPreguntas.toFloat()) * 10).toInt()
        } else {
            0
        }

        Log.d("ActualizarLeccion", "Dificultad detectada: $dificultadEjercicio, Total preguntas: $totalPreguntas, Respuestas correctas: $puntajeFinal, Puntaje enviado: $puntajeEnvio")

        val estadoFinal = true
        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime

        CoroutineScope(Dispatchers.IO).launch {
            val leccionDao = AppDatabase.getDatabase(applicationContext).leccionDao()

            leccionDao.updateLessonDurationAndCompletionDate(idLeccion, duration, endTime, startTime)
            leccionDao.updateLeccion(idLeccion, estadoFinal, puntajeEnvio)

            Log.d("GuardarDuracionLeccion", "Duración: $duration ms, Tiempo de finalización: $endTime ms, Tiempo de inicio: $startTime ms")
            Log.d("ActualizarLeccion", "Lección actualizada: ID = $idLeccion, Puntaje = $puntajeEnvio, Estado = $estadoFinal")
        }
    }

    private fun actualizarProgreso(totalPreguntas: Int) {
        indexProgres++
        val progreso = ((indexProgres) * 100) / totalPreguntas

        val animator = ObjectAnimator.ofInt(progressBar, "progress", progressBar.progress, progreso)
        animator.duration = 500
        animator.interpolator = DecelerateInterpolator()
        animator.start()
    }

    private fun mostrarPuntajeFinal() {
        tvPuntajeFinal.visibility = View.VISIBLE
        imagenResultado.visibility = View.VISIBLE
        tvPuntajeFinal.alpha = 0f
        tvPuntajeFinal.text = "Puntaje Final: 0"

        val startValue = 0
        val endValue = score

        val animator = ValueAnimator.ofInt(startValue, endValue)
        animator.duration = 2000
        animator.addUpdateListener { animation ->
            val animatedValue = animation.animatedValue as Int
            tvPuntajeFinal.text = "Puntaje Final: $animatedValue"
        }
        animator.start()

        tvPuntajeFinal.animate()
            .alpha(1f)
            .setDuration(1000)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()

        val estrellas = calcularEstrellas()
        mostrarEstrellas(estrellas)

        Handler(Looper.getMainLooper()).postDelayed({
            finish()
        }, 5000)
    }

    private fun mostrarEstrellas(estrellas: Int) {
        when (estrellas) {
            3 -> {
                imageViewEstrellas.setImageResource(R.drawable.estrella3)
                val imagenResultado: ImageView = findViewById(R.id.imagenResultado)
                imagenResultado.setImageResource(R.drawable.excelente)
            }
            2 -> {
                imageViewEstrellas.setImageResource(R.drawable.estrella2)
                imagenResultado.setImageResource(R.drawable.excelente)
            }
            1 -> {
                imageViewEstrellas.setImageResource(R.drawable.estrella1)
                imagenResultado.setImageResource(R.drawable.excelente)
            }
            0 -> {
                imageViewEstrellas.setImageResource(R.drawable.vuelveintentar)
                imagenResultado.setImageResource(R.drawable.losentimos)
            }
        }

        imageViewEstrellas.alpha = 0f
        imageViewEstrellas.visibility = View.VISIBLE
        imageViewEstrellas.animate()
            .alpha(1f)
            .setDuration(500)
            .start()
    }

    private fun calcularEstrellas(): Int {
        val porcentajeAciertos = if (totalPreguntas > 0) {
            (score.toFloat() / totalPreguntas.toFloat()) * 100
        } else {
            0f
        }

        return when {
            porcentajeAciertos >= 80 -> 3
            porcentajeAciertos >= 60 -> 2
            porcentajeAciertos >= 40 -> 1
            else -> 0
        }
    }
}