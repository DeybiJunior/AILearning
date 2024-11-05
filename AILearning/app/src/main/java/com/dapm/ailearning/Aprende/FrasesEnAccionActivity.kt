package com.dapm.ailearning.Aprende

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
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
import androidx.lifecycle.lifecycleScope
import com.dapm.ailearning.Aprende.CompletaFrases.LeccionCompletarJson
import com.dapm.ailearning.Aprende.CompletaFrases.QuizCompletar
import com.dapm.ailearning.Datos.AppDatabase
import com.dapm.ailearning.Datos.LeccionDao
import com.dapm.ailearning.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FrasesEnAccionActivity : AppCompatActivity()  {
    private lateinit var leccionDao: LeccionDao
    private lateinit var lessonContent: LeccionCompletarJson
    private var currentQuestionIndex = 0
    private var score = 0 // Variable para almacenar el puntaje
    private lateinit var scoreTextView: TextView
    private lateinit var progressBar: ProgressBar // Variable para el ProgressBar
    private var indexProgres = 0 // Cambié el nombre de Indexprogres a indexProgres
    private lateinit var closeImageView: ImageView
    private lateinit var tvPuntajeFinal: TextView // Asegúrate de tener esta variable para el puntaje final
    private lateinit var imageViewEstrellas: ImageView
    private lateinit var imagenResultado: ImageView
    private lateinit var cardimag: CardView
    private lateinit var container: LinearLayout
    private var idLeccion: Int = -1

    private var startTime: Long = 0
    private var endTime: Long = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_desafio_comprension)

        startTime = System.currentTimeMillis()

        // Inicializa los componentes
        scoreTextView = findViewById(R.id.tvPuntaje)
        progressBar = findViewById(R.id.progressBar) // Inicializa el ProgressBar
        tvPuntajeFinal = findViewById(R.id.tvPuntajeFinal) // Inicializa el TextView para el puntaje final
        imageViewEstrellas = findViewById(R.id.imageViewEstrellas)
        imagenResultado = findViewById(R.id.imagenResultado) // Asegúrate de que el ID sea correcto
        cardimag = findViewById(R.id.cardimag)
        container = findViewById(R.id.container)

        initializeDatabase() // Llama a la función de inicialización

        closeImageView = findViewById(R.id.clouse)
        closeImageView.setOnClickListener {
            finish()
        }
        idLeccion = intent.getIntExtra("idLeccion", -1)
        if (idLeccion != -1) {
            loadLesson(idLeccion)
        }
    }


    // Función para inicializar leccionDao
    private fun initializeDatabase() {
        val db = AppDatabase.getDatabase(applicationContext) // Usa el método getDatabase
        leccionDao = db.leccionDao() // Inicializa leccionDao
    }

    private fun loadLesson(lessonId: Int) {
        lifecycleScope.launch {
            val json = leccionDao.getJsonByLessonId(lessonId)
            val lessonContentList: List<LeccionCompletarJson> = Gson().fromJson(json,
                object : TypeToken<List<LeccionCompletarJson>>() {}.type)

            // Supongamos que solo tomamos el primer contenido de la lista
            lessonContent = lessonContentList.first()

            // Muestra el contenido de lectura
            showReading("Completa las frases con la palabra correcta")
        }
    }

    private fun showReading(reading: String) {
        val readingTextView: TextView = findViewById(R.id.reading_text_view)
        val continueButton: Button = findViewById(R.id.continue_button)
        readingTextView.text = reading

        continueButton.setOnClickListener {
            // Una vez que el usuario presione continuar, mostramos el quiz
            showQuiz(lessonContent.quiz)
            readingTextView.visibility = View.GONE
            continueButton.visibility = View.GONE
        }
    }

    private fun showQuiz(quizList: List<QuizCompletar>) {
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
                        score=score+2 // Incrementa el puntaje por respuesta correcta
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

        questionView.visibility = View.GONE
        optionsGroup.visibility = View.GONE
        submitButton.visibility = View.GONE
        cardimag.visibility = View.GONE
        container.visibility = View.GONE
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

        actualizarLeccion(endValue) // Cambié aquí
        guardarDuracionLeccion()
        Handler(Looper.getMainLooper()).postDelayed({
            finish()
        }, 5000)
    }


    private fun guardarDuracionLeccion() {
        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime

        CoroutineScope(Dispatchers.IO).launch {
            val leccionDao = AppDatabase.getDatabase(applicationContext).leccionDao() // Obtén una instancia de tu DAO

            // Actualiza los datos de la lección en la base de datos, incluyendo startTime
            leccionDao.updateLessonDurationAndCompletionDate(idLeccion, duration, endTime, startTime)

            // Log para verificar los valores de duración y tiempo de finalización
            Log.d("GuardarDuracionLeccion", "Duración: $duration ms, Tiempo de finalización: $endTime ms, Tiempo de inicio: $startTime ms")
        }
    }


    private fun actualizarLeccion(puntajeFinal: Int) {
        val estadoFinal = true // Estado de la lección completada

        // Crear una corrutina para actualizar la lección en la base de datos
        CoroutineScope(Dispatchers.IO).launch {
            val leccionDao = AppDatabase.getDatabase(applicationContext).leccionDao() // Obtén una instancia de tu DAO

            // Actualiza la lección usando la propiedad de clase
            leccionDao.updateLeccion(idLeccion, estadoFinal, puntajeFinal)

            // Log para confirmar la actualización
            Log.d("ActualizarLeccion", "Lección actualizada: ID = $idLeccion, Puntaje = $puntajeFinal, Estado = $estadoFinal")
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
        quizList: List<QuizCompletar>,
        index: Int,
        questionView: TextView,
        optionsGroup: RadioGroup
    ) {
        val currentQuiz = quizList[index]
        questionView.text = currentQuiz.frase

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
