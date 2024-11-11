package com.dapm.ailearning.Aprende

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.dapm.ailearning.Aprende.Adivina.AdivinaJson
import com.dapm.ailearning.Datos.AppDatabase
import com.dapm.ailearning.R
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AdivinaPalabraActivity : AppCompatActivity() {

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
    private lateinit var  pista: TextView
    private var startTime: Long = 0
    private var endTime: Long = 0


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


        idLeccion = intent.getIntExtra("idLeccion", -1)
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

        // Asegúrate de que la longitud de userGuess sea igual a la longitud de correctAnswer
        if (userGuess.length != correctAnswer.length) {
            feedbackTextView.text = "La palabra debe tener ${correctAnswer.length} letras."
            return
        }

        if (userGuess.equals(correctAnswer, ignoreCase = true)) {
            feedbackTextView.text = "¡Correcto!"
            guessEditText.isEnabled = false

            score++
            tvPuntaje.text = "Puntaje: $score"

            // Cambiar todos los botones a verde si la respuesta es correcta
            val buttonsCount = letterButtonsLayout.childCount
            for (i in 0 until buttonsCount) {
                val button = letterButtonsLayout.getChildAt(i) as Button
                button.setBackgroundColor(ContextCompat.getColor(this, R.color.basicoColor))
                button.text = correctAnswer[i].toString() // Muestra la letra correcta
            }
            return
        } else {
            failedAttempts--
            feedbackTextView.text = "Intenta de nuevo, la palabra no es la correcta, te quedan $failedAttempts intentos ."

            if (failedAttempts <= 0) {
                guessEditText.isEnabled = false
                feedbackTextView.text = "Has agotado tus intentos. La palabra era \"$correctAnswer\""
                return
            }

            // Cambia colores de botones según el guess
            val buttonsCount = letterButtonsLayout.childCount
            val colorMap = mutableMapOf<Char, Int>()


            // Contar letras correctas
            for (i in correctAnswer.indices) {
                val button = letterButtonsLayout.getChildAt(i) as Button
                if (userGuess[i].equals(correctAnswer[i], ignoreCase = true)) {
                    button.setBackgroundColor(ContextCompat.getColor(this, R.color.basicoColor))
                    button.text = userGuess[i].toString() // Muestra la letra correcta
                    colorMap[userGuess[i]] = 1 // Marcar letra como correcta
                } else {
                    colorMap[userGuess[i]] = 0 // Marcar letra como incorrecta
                }
            }

            // Cambia a amarillo si la letra está en la palabra pero en la posición incorrecta
            for (i in correctAnswer.indices) {
                if (userGuess[i] != correctAnswer[i] && correctAnswer.contains(userGuess[i])) {
                    val button = letterButtonsLayout.getChildAt(i) as Button
                    if (colorMap[userGuess[i]] == 0) {
                        button.setBackgroundColor(ContextCompat.getColor(this, R.color.intermedioColor))
                        button.text = userGuess[i].toString() // Muestra la letra
                    }
                }
            }

            for (i in 0 until buttonsCount) {
                val button = letterButtonsLayout.getChildAt(i) as Button
                if (button.text == "_") {
                    button.setBackgroundColor(Color.GRAY) // Gris si no hay coincidencias
                }
            }
        }
    }


    private fun nextQuestion() {
        if (currentQuestionIndex < frases.size - 1) { // Asegúrate de que no se salga de los límites
            currentQuestionIndex++ // Incrementa el índice antes de mostrar la nueva pista
            clueTextView.text = frases[currentQuestionIndex].clue
            guessEditText.text.clear() // Limpia el campo de entrada
            guessEditText.isEnabled = true
            failedAttempts = 10
            feedbackTextView.text = "" // Limpia el mensaje de retroalimentación
            actualizarProgreso(totalPreguntas)
            initializeLetterButtons(frases[currentQuestionIndex].oneword.length)


        } else {
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

            mostrarPuntajeFinal()
            actualizarLeccion(score)
        }
    }

    private fun actualizarLeccion(puntajeFinal: Int) {
        // Log to confirm startTime
        Log.d("ActualizarLeccion", "startTime at call: $startTime")

        // Determine the score to send based on the final score value
        val puntajeEnvio = when {
            puntajeFinal >= 3 -> 10 // Si puntajeFinal es mayor a 3, asignar 10
            puntajeFinal == 2 -> 8
            puntajeFinal == 1 -> 5
            puntajeFinal == 0 -> 0
            else -> 0
        }

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
        tvPuntajeFinal.alpha = 0f // Comienza invisible
        tvPuntajeFinal.text = "Puntaje Final: 0" // Comienza en 0

        val startValue = 0
        val endValue = score // Usamos la variable de puntaje calculada

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

        Handler(Looper.getMainLooper()).postDelayed({
            finish()
        }, 5000)
    }


    private fun mostrarEstrellas(estrellas: Int) {
        // Cambiar la imagen según el puntaje
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
            .alpha(1f) // Animar a alpha 1 (visible)
            .setDuration(500) // Duración de la animación
            .start() // Iniciar la animación
    }

    private fun calcularEstrellas(): Int {
        return when {
            score >= 3 -> 3 // 3 estrellas
            score >= 2 -> 2 // 2 estrellas
            score >= 1 -> 1 // 1 estrella
            else -> 0 // Sin estrellas
        }
    }




}


