package com.dapm.ailearning.Aprende

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import com.dapm.ailearning.Aprende.DateQuiz.LeccionJson
import com.dapm.ailearning.Aprende.DateQuiz.Quiz
import android.animation.ValueAnimator
import android.annotation.SuppressLint
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
import com.dapm.ailearning.Datos.AppDatabase
import com.dapm.ailearning.Datos.LeccionDao
import com.dapm.ailearning.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DesafioCartasActivity : AppCompatActivity() {
    private lateinit var leccionDao: LeccionDao
    private lateinit var lessonContent: LeccionJson
    private var currentQuestionIndex = 0
    private var score = 0 // Variable para almacenar el puntaje
    private lateinit var scoreTextView: TextView
    private lateinit var progressBar: ProgressBar // Variable para el ProgressBar
    private var indexProgres = 0 // Cambié el nombre de Indexprogres a indexProgres
    private lateinit var closeImageView: ImageView
    private lateinit var tvPuntajeFinal: TextView // Asegúrate de tener esta variable para el puntaje final
    private lateinit var imageViewEstrellas: ImageView
    private lateinit var imagenResultado: ImageView
    private lateinit var cardflip: CardView
    private lateinit var container: LinearLayout
    private var idLeccion: Int = -1
    private lateinit var linearLayoutcard: LinearLayout

    //Voltear cartas
    private lateinit var front_anim: AnimatorSet
    private lateinit var back_anim: AnimatorSet
    private var isFront =true
    private lateinit var front: TextView
    private lateinit var back: TextView

    private var startTime: Long = 0
    private var endTime: Long = 0

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_desafio_cartas)
        startTime = System.currentTimeMillis()
        // Now Create Animator Object
        // For this we add animator folder inside res
        // Now we will add the animator to our card
        // we now need to modify the camera scale
        var scale = applicationContext.resources.displayMetrics.density

        front = findViewById(R.id.card_front)
        back = findViewById(R.id.card_back)

        front.cameraDistance = 8000 * scale
        back.cameraDistance = 8000 * scale

        // Now we will set the front animation
        front_anim = AnimatorInflater.loadAnimator(applicationContext, R.animator.front_animator) as AnimatorSet
        back_anim = AnimatorInflater.loadAnimator(applicationContext, R.animator.back_animator) as AnimatorSet
        linearLayoutcard = findViewById(R.id.linearLayoutcard)

        // Inicializa los componentes
        scoreTextView = findViewById(R.id.tvPuntaje)
        progressBar = findViewById(R.id.progressBar) // Inicializa el ProgressBar
        tvPuntajeFinal = findViewById(R.id.tvPuntajeFinal) // Inicializa el TextView para el puntaje final
        imageViewEstrellas = findViewById(R.id.imageViewEstrellas)
        imagenResultado = findViewById(R.id.imagenResultado) // Asegúrate de que el ID sea correcto
        cardflip = findViewById(R.id.card_flip)
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
            val lessonContentList: List<LeccionJson> = Gson().fromJson(json, object : TypeToken<List<LeccionJson>>() {}.type)

            // Get the first lesson from the list
            lessonContent = lessonContentList.first()

            // Now show the quiz
            showQuiz(lessonContent.quiz.toMutableList())
        }
    }



    private fun showQuiz(quizList: MutableList<Quiz>) {
        val questionView: TextView = findViewById(R.id.question_text_view)
        val optionsGroup: RadioGroup = findViewById(R.id.options_radio_group)
        val submitButton: Button = findViewById(R.id.submit_button)
        val cardButton: Button = findViewById(R.id.card_button)
        val cardFront: TextView = findViewById(R.id.card_front)
        val cardBack: TextView = findViewById(R.id.card_back)
        var acerto: Boolean = true;
        // Verifica si hay preguntas
        if (quizList.isNotEmpty()) {
            // Cargar la pregunta actual
            loadQuestion(quizList, currentQuestionIndex, questionView, optionsGroup)

            questionView.visibility = View.VISIBLE
            optionsGroup.visibility = View.VISIBLE
            submitButton.visibility = View.VISIBLE
            cardButton.visibility = View.GONE
            linearLayoutcard.visibility = View.VISIBLE

            cardFront.text="?"
            submitButton.setOnClickListener {
                val selectedId = optionsGroup.checkedRadioButtonId
                val selectedOption = findViewById<RadioButton>(selectedId)

                if (selectedOption != null) {
                    optionsGroup.visibility = View.GONE
                    cardBack.text = "Respuesta Correcta: \n" + quizList[currentQuestionIndex].correct_answer
                    // Verifica la respuesta
                    if (selectedOption.text == quizList[currentQuestionIndex].correct_answer) {
                        score++ // Incrementa el puntaje por respuesta correcta
                        scoreTextView.text = "Puntaje: $score" // Actualiza el TextView del puntaje
                        acerto=true;
                    } else {
                        // Si la respuesta es incorrecta, mover la pregunta al final
                        val incorrectQuestion = quizList.removeAt(currentQuestionIndex)
                        quizList.add(incorrectQuestion)
                        acerto=false;
                    }

                    // Animaciones de la tarjeta
                    if (isFront) {
                        front_anim.setTarget(front)
                        back_anim.setTarget(back)
                        front_anim.start()
                        back_anim.start()
                        isFront = false
                        cardButton.visibility = View.VISIBLE
                        submitButton.visibility = View.GONE
                    }
                } else {
                    Toast.makeText(this, "Por favor selecciona una opción.", Toast.LENGTH_SHORT).show()
                }
            }

            cardButton.setOnClickListener {
                // Independientemente de si la respuesta fue correcta o no, avanzamos a la siguiente pregunta
                if (acerto) {
                    currentQuestionIndex++// Incrementa el índice para la siguiente pregunta
                    actualizarProgreso(10) // Actualiza el progreso
                }
                optionsGroup.visibility = View.VISIBLE
                if (!isFront) {
                    front_anim.setTarget(back)
                    back_anim.setTarget(front)
                    back_anim.start()
                    front_anim.start()
                    isFront = true
                    cardButton.visibility = View.GONE
                    submitButton.visibility = View.VISIBLE
                }
                if (currentQuestionIndex < quizList.size) {
                    loadQuestion(quizList, currentQuestionIndex, questionView, optionsGroup)
                } else {
                    linearLayoutcard.visibility = View.GONE
                    hideQuizViews()
                    mostrarPuntajeFinal()
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
        cardflip.visibility = View.GONE
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