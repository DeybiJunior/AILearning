package com.dapm.ailearning.Perfil

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.dapm.ailearning.Datos.AppDatabase
import com.dapm.ailearning.Datos.LeccionDao
import com.dapm.ailearning.Login.InicioSesionActivity
import com.dapm.ailearning.R
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch


class PerfilFragment : Fragment() {
    private lateinit var leccionDao: LeccionDao // Asegúrate de inicializar esto adecuadamente

    private lateinit var auth: FirebaseAuth
    private lateinit var loginCard: CardView
    private lateinit var logoutCard: CardView
    private lateinit var circularProgressIndicator: CircularProgressIndicator
    private lateinit var progressText: TextView
    private lateinit var textViewDocenteSeleccionado: TextView // Añadir la variable para el TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        circularProgressIndicator = view.findViewById(R.id.circularProgressIndicator)
        progressText = view.findViewById(R.id.progressText)
        textViewDocenteSeleccionado = view.findViewById(R.id.textViewDocenteSeleccionado) // Inicializar el TextView


        val database = AppDatabase.getDatabase(requireContext())
        leccionDao = database.leccionDao()

        updateLevelBasedOnLessons()

        val nivel = getUserLevel() // Obtén el nivel o 0 si no existe
        updateProgress(nivel)
        increaseProgress(nivel) // Pasa el nivel recuperado
    }

    override fun onResume() {
        super.onResume()
        cargarNombreDocente()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        auth = FirebaseAuth.getInstance()

        val view = inflater.inflate(R.layout.fragment_perfil, container, false)

        loginCard = view.findViewById(R.id.loginCard)
        logoutCard = view.findViewById(R.id.logoutCard)

        val currentUser = auth.currentUser
        if (currentUser == null) {
            loginCard.visibility = View.VISIBLE
            loginCard.setOnClickListener {
                handleLogin()
            }
        } else {
            loginCard.visibility = View.GONE
            logoutCard.visibility = View.VISIBLE
            logoutCard.setOnClickListener {
                handleLogout()
            }
        }

        val cardViewSeleccionDocente: CardView = view.findViewById(R.id.cardViewSeleccionDocente)
        cardViewSeleccionDocente.setOnClickListener {
            val intent = Intent(requireContext(), SeleccionDocenteActivity::class.java)
            startActivity(intent)
        }

        circularProgressIndicator = view.findViewById(R.id.circularProgressIndicator)
        progressText = view.findViewById(R.id.progressText)

        updateProgress(0)

        return view
    }

    private fun cargarNombreDocente() {
        val sharedPref = activity?.getSharedPreferences("mis_preferencias", android.content.Context.MODE_PRIVATE)
        val nombreDocente = sharedPref?.getString("nombre_docente", null) // Obtener el nombre del docente
        val apellidoDocente = sharedPref?.getString("apellido_docente", null) // Obtener el apellido del docente

        // Mostrar el nombre completo o "Ninguno" si no hay datos
        if (!nombreDocente.isNullOrEmpty() && !apellidoDocente.isNullOrEmpty()) {
            textViewDocenteSeleccionado.text = "$nombreDocente $apellidoDocente"
        } else {
            textViewDocenteSeleccionado.text = "Ninguno"
        }
    }
    private fun getUserLevel(): Int {
        val sharedPref = activity?.getSharedPreferences("mis_preferencias", android.content.Context.MODE_PRIVATE)
        return sharedPref?.getInt("user_nivel", 0) ?: 0 // Obtén el nivel o 0 si no existe
    }

    private fun updateLevelBasedOnLessons() {
        val userId = auth.currentUser?.uid ?: return

        // Ejecutar la consulta en un hilo de fondo
        lifecycleScope.launch {
            val completedLessons = leccionDao.getCompletedLessonCount(userId)
            val newLevel = calculateLevel(completedLessons)

            // Guardar el nivel en SharedPreferences
            saveUserLevel(newLevel)

            // Actualizar la interfaz de usuario con el nuevo nivel
            updateProgress(newLevel)
            increaseProgress(newLevel)
        }
    }

    // Actualiza el cálculo del nivel para permitir más niveles
    private fun calculateLevel(completedLessonCount: Int): Int {
        // Ejemplo: 1 nivel por cada 5 lecciones completadas, máximo 10 niveles
        return (completedLessonCount / 5).coerceAtMost(10) // Ajustado para más niveles
    }

    private fun saveUserLevel(level: Int) {
        val sharedPref = activity?.getSharedPreferences("mis_preferencias", android.content.Context.MODE_PRIVATE)
        sharedPref?.edit()?.putInt("user_nivel", level)?.apply()
    }

    // Modificar updateProgress para actualizar en porcentaje
    private fun updateProgress(nivel: Int) {
        val progress = (nivel * 10).coerceAtMost(100) // Ajuste para reflejar el nivel de 0 a 10
        circularProgressIndicator.setProgressCompat(progress, true)
        progressText.text = "Nivel: $nivel"
    }

    // Ejemplo de cómo variar el progreso
    private fun increaseProgress(currentProgress: Int) {
        var progress = 0
        // Progreso máximo para la animación
        val maxProgress = 10

        // Limitar el progreso actual a un máximo de 10
        val targetProgress = currentProgress.coerceAtMost(maxProgress)

        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed(object : Runnable {
            override fun run() {
                if (progress < targetProgress) {
                    progress += 1
                    // Calculamos el porcentaje de progreso
                    updateProgress(currentProgress) // Mantener el nivel actual sin cambiar
                    handler.postDelayed(this, 30) // Actualiza cada 30ms
                } else {
                    // Asegúrate de mostrar el nivel final al terminar la animación
                    updateProgress(currentProgress)
                }
            }
        }, 500)
    }

    private fun handleLogin() {
        val intent = Intent(requireContext(), InicioSesionActivity::class.java)
        startActivity(intent)
    }

    private fun handleLogout() {
        auth.signOut()
        val intent = Intent(requireContext(), InicioSesionActivity::class.java)
        startActivity(intent)
        activity?.finish()
    }
}
