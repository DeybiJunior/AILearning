package com.dapm.ailearning.Aprende

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Spannable
import android.text.SpannableString
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
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
import androidx.core.content.ContextCompat
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
    private lateinit var container: LinearLayout
    private var idLeccion: Int = -1
    private lateinit var cardimag2: CardView

    private lateinit var constraint: ConstraintLayout
    private var startTime: Long = 0
    private var endTime: Long = 0
    private lateinit var constraintLayout: ConstraintLayout
    private lateinit var completo1: LinearLayout


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_frases_en_accion)

        startTime = System.currentTimeMillis()

        // Inicializa los componentes
        scoreTextView = findViewById(R.id.tvPuntaje)
        progressBar = findViewById(R.id.progressBar) // Inicializa el ProgressBar
        tvPuntajeFinal = findViewById(R.id.tvPuntajeFinal) // Inicializa el TextView para el puntaje final
        imageViewEstrellas = findViewById(R.id.imageViewEstrellas)
        imagenResultado = findViewById(R.id.imagenResultado) // Asegúrate de que el ID sea correcto
        cardimag2 = findViewById(R.id.cardimag2)
        container = findViewById(R.id.container)
        container.visibility = View.VISIBLE
        constraint = findViewById(R.id.constraint)
        constraint.visibility = View.VISIBLE
        constraintLayout = findViewById(R.id.constraintLayout)
        constraintLayout.visibility = View.GONE
        completo1 = findViewById(R.id.completo1)
        completo1.visibility = View.VISIBLE

        initializeDatabase() // Llama a la función de inicialización

        closeImageView = findViewById(R.id.clouse)
        closeImageView.setOnClickListener {
            finish()
        }
        idLeccion = intent.getIntExtra("idLeccion", -1)

        // Reinicio de Respuestas seleccionadas
        CoroutineScope(Dispatchers.IO).launch {
            leccionDao.updateRespuestasSeleccionadas(idLeccion, "")
        }

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
            showQuiz(lessonContent.quiz)
            cardimag2.visibility = View.VISIBLE
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

            // Listener for changes in the selected option in the RadioGroup
            optionsGroup.setOnCheckedChangeListener { _, checkedId ->
                val selectedOption = findViewById<RadioButton>(checkedId)
                if (selectedOption != null) {
                    // Original phrase with underscore
                    val originalFrase = quizList[currentQuestionIndex].frase

                    // Find the position of the underscore in the phrase
                    val underscoreIndex = originalFrase.indexOf("_")
                    if (underscoreIndex != -1) {
                        // Replace the underscore with the selected option's text
                        val updatedText = originalFrase.replace("_", selectedOption.text.toString())

                        // Create a SpannableString to apply styles
                        val spannable = SpannableString(updatedText)

                        // Calculate the end position of the selected option text
                        val endOfBoldText = underscoreIndex + selectedOption.text.length

                        // Apply bold style to the selected text
                        spannable.setSpan(
                            StyleSpan(Typeface.BOLD), // Set the style to bold
                            underscoreIndex, // Start position of the text to be bold
                            endOfBoldText, // End position of the bold text
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )

                        // Apply text size (16sp) to the selected text
                        spannable.setSpan(
                            AbsoluteSizeSpan(20, true), // Set size to 16sp
                            underscoreIndex,
                            endOfBoldText,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )

                        val highlightColor = ContextCompat.getColor(this, R.color.holo_green_ligth)

                        // Apply color to the selected text
                        spannable.setSpan(
                            ForegroundColorSpan(highlightColor),
                            underscoreIndex,
                            endOfBoldText,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )

                        // Update the question TextView with the styled text
                        questionView.text = spannable
                    }
                }
            }

            submitButton.setOnClickListener {
                val selectedId = optionsGroup.checkedRadioButtonId
                val selectedOption = findViewById<RadioButton>(selectedId)

                if (selectedOption != null) {

                    //Repuestas seleccionadas
                    CoroutineScope(Dispatchers.Main).launch {
                        leccionDao.agregarRespuestasSeleccionadas(idLeccion, selectedOption.text.toString())
                    }

                    val correctAnswer = quizList[currentQuestionIndex].correct_answer
                    if (selectedOption.text == correctAnswer) {
                        // If the answer is correct
                        score += 2 // Increment the score for a correct answer
                        scoreTextView.text = "Puntaje: $score" // Update the score TextView
                        Toast.makeText(this, "Correcto!", Toast.LENGTH_SHORT).show()
                    } else {
                        // If the answer is incorrect
                        Toast.makeText(this, "Incorrecto! La respuesta era: $correctAnswer", Toast.LENGTH_SHORT).show()
                    }

                    // Move to the next question
                    currentQuestionIndex++ // Increment the index for the next question
                    actualizarProgreso(quizList.size) // Update the progress

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

    private fun hideQuizViews() {
        val questionView: TextView = findViewById(R.id.question_text_view)
        val optionsGroup: RadioGroup = findViewById(R.id.options_radio_group)
        val submitButton: Button = findViewById(R.id.submit_button)

        questionView.visibility = View.GONE
        optionsGroup.visibility = View.GONE
        submitButton.visibility = View.GONE
        container.visibility = View.GONE
    }

    private fun mostrarPuntajeFinal() {
        completo1.visibility = View.GONE
        constraint.visibility = View.GONE
        constraintLayout.visibility = View.VISIBLE
        cardimag2.visibility = View.GONE
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
        // Log to confirm startTime
        Log.d("ActualizarLeccion", "startTime at call: $startTime")
        // Determine the score to send based on the final score value
        val puntajeEnvio = when {
            puntajeFinal >= 3 -> 10
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
