package com.dapm.ailearning.Aprende

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.dapm.ailearning.Aprende.DateQuiz.LeccionJson
import com.dapm.ailearning.Aprende.DateQuiz.Quiz
import com.dapm.ailearning.Datos.AppDatabase
import com.dapm.ailearning.Datos.LeccionDao
import com.dapm.ailearning.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class DesafioComprensionActivity : AppCompatActivity() {
    private lateinit var leccionDao: LeccionDao
    private lateinit var lessonContent: LeccionJson
    private var currentQuestionIndex = 0
    private var score = 0
    private lateinit var scoreTextView: TextView
    private lateinit var progressBar: ProgressBar
    private var indexProgres = 0
    private lateinit var closeImageView: ImageView
    private lateinit var tvPuntajeFinal: TextView
    private lateinit var imageViewEstrellas: ImageView
    private lateinit var imagenResultado: ImageView
    private lateinit var cardimag: CardView
    private lateinit var container: LinearLayout
    private lateinit var constraint: ConstraintLayout
    private lateinit var completo1: LinearLayout
    private lateinit var cardimag2: CardView
    private lateinit var readingTextView: TextView
    private lateinit var constraintLayout: ConstraintLayout

    private var idLeccion: Int = -1

    private var startTime: Long = 0
    private var endTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_desafio_comprension)

        startTime = System.currentTimeMillis()

        // Initialize all views here
        scoreTextView = findViewById(R.id.tvPuntaje)
        progressBar = findViewById(R.id.progressBar)
        tvPuntajeFinal = findViewById(R.id.tvPuntajeFinal)
        imageViewEstrellas = findViewById(R.id.imageViewEstrellas)
        imagenResultado = findViewById(R.id.imagenResultado)
        cardimag = findViewById(R.id.cardimag)
        cardimag2 = findViewById(R.id.cardimag2)
        container = findViewById(R.id.container)
        constraint = findViewById(R.id.constraint)
        completo1 = findViewById(R.id.completo1)
        readingTextView = findViewById(R.id.reading_text_view)
        constraintLayout = findViewById(R.id.constraintLayout)

        constraintLayout.visibility = View.GONE
        readingTextView.visibility = View.VISIBLE
        cardimag2.visibility = View.GONE

        initializeDatabase()



        closeImageView = findViewById(R.id.clouse)
        closeImageView.setOnClickListener {
            finish()
        }

        idLeccion = intent.getIntExtra("idLeccion", -1)
        if (idLeccion != -1) {
            loadLesson(idLeccion)
        }
    }

    private fun initializeDatabase() {
        val db = AppDatabase.getDatabase(applicationContext) // Usa el método getDatabase
        leccionDao = db.leccionDao() // Inicializa leccionDao
    }

    private fun loadLesson(lessonId: Int) {
        lifecycleScope.launch {
            val json = leccionDao.getJsonByLessonId(lessonId)
            val lessonContentList: List<LeccionJson> = Gson().fromJson(json,
                object : TypeToken<List<LeccionJson>>() {}.type)

            // Supongamos que solo tomamos el primer contenido de la lista
            lessonContent = lessonContentList.first()

            // Muestra el contenido de lectura
            showReading(lessonContent.reading)
        }
    }

    private fun showReading(reading: String) {
        val readingTextView: TextView = findViewById(R.id.reading_text_view)
        val continueButton: Button = findViewById(R.id.continue_button)
        readingTextView.text = reading

        continueButton.setOnClickListener {
            // Una vez que el usuario presione continuar, mostramos el quiz
            showQuiz(lessonContent.quiz)
            cardimag2.visibility = View.VISIBLE
            readingTextView.visibility = View.GONE
            continueButton.visibility = View.GONE

            cardimag.setOnClickListener {
                activartexto()
            }
        }


    }

    private fun activartexto() {
        readingTextView.visibility = if (readingTextView.visibility == View.VISIBLE) {
            View.GONE
        } else {
            View.VISIBLE
        }
    }

    private fun showQuiz(quizList: List<Quiz>) {
        val questionView: TextView = findViewById(R.id.question_text_view)
        val optionsGroup: RadioGroup = findViewById(R.id.options_radio_group)
        val submitButton: Button = findViewById(R.id.submit_button)

        // Verifica si hay preguntas
        if (quizList.isNotEmpty()) {
            // Cargar la pregunta actual
            loadQuestion(quizList, currentQuestionIndex, questionView, optionsGroup)

            questionView.visibility = View.VISIBLE
            optionsGroup.visibility = View.VISIBLE
            submitButton.visibility = View.VISIBLE

            submitButton.setOnClickListener {
                val selectedId = optionsGroup.checkedRadioButtonId
                val selectedOption = findViewById<RadioButton>(selectedId)
                if (selectedOption != null) {
                    // Verifica la respuesta
                    if (selectedOption.text == quizList[currentQuestionIndex].correct_answer) {
                        score++ // Incrementa el puntaje por respuesta correcta
                        scoreTextView.text = "Puntaje: $score" // Actualiza el TextView del puntaje
                    }

                    // Independientemente de si la respuesta fue correcta o no, avanzamos a la siguiente pregunta
                    currentQuestionIndex++ // Incrementa el índice para la siguiente pregunta
                    actualizarProgreso(quizList.size) // Actualiza el progreso

                    if (currentQuestionIndex < quizList.size) {
                        loadQuestion(quizList, currentQuestionIndex, questionView, optionsGroup)
                    } else {
                        hideQuizViews()
                        mostrarPuntajeFinal()
                    }
                } else {
                    Toast.makeText(this, "Por favor selecciona una opción.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun hideQuizViews() {
        val questionView: TextView = findViewById(R.id.question_text_view)
        val optionsGroup: RadioGroup = findViewById(R.id.options_radio_group)
        val submitButton: Button = findViewById(R.id.submit_button)

        cardimag2.visibility = View.GONE
        completo1.visibility = View.GONE
        constraint.visibility = View.GONE
        questionView.visibility = View.GONE
        optionsGroup.visibility = View.GONE
        submitButton.visibility = View.GONE
        cardimag.visibility = View.GONE
        container.visibility = View.GONE
    }

    private fun mostrarPuntajeFinal() {
        constraintLayout.visibility = View.VISIBLE
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

        actualizarLeccion(endValue) // Cambié aquí
        Handler(Looper.getMainLooper()).postDelayed({
            finish()
        }, 5000)
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





    // Función para cargar la pregunta en la UI
    private fun loadQuestion(
        quizList: List<Quiz>,
        index: Int,
        questionView: TextView,
        optionsGroup: RadioGroup
    ) {
        val currentQuiz = quizList[index]
        questionView.text = currentQuiz.question

        // Limpia las opciones anteriores
        optionsGroup.removeAllViews()
        currentQuiz.options.forEach { option ->
            val radioButton = RadioButton(this)
            radioButton.text = option
            optionsGroup.addView(radioButton)
        }
    }

    // Función para actualizar el progreso
    private fun actualizarProgreso(totalPreguntas: Int) {
        indexProgres++
        val progreso = ((indexProgres) * 100) / totalPreguntas

        val animator = ObjectAnimator.ofInt(progressBar, "progress", progressBar.progress, progreso)
        animator.duration = 500
        animator.interpolator = DecelerateInterpolator()
        animator.start()
    }



}
