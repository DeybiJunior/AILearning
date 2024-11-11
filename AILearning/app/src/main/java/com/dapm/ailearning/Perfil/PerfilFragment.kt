package com.dapm.ailearning.Perfil

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
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
    private lateinit var textViewDocenteSeleccionado: TextView
    private lateinit var textViewdificultad: TextView
    private lateinit var leccionesCompletasText: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        circularProgressIndicator = view.findViewById(R.id.circularProgressIndicator)
        progressText = view.findViewById(R.id.progressText)
        textViewDocenteSeleccionado = view.findViewById(R.id.textViewDocenteSeleccionado)
        textViewdificultad = view.findViewById(R.id.textViewdificultad)
        leccionesCompletasText = view.findViewById(R.id.leccionesCompletasText)

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
        cargarDificultad()
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

        val cardViewDificultad: CardView = view.findViewById(R.id.cardViewDificultad)
        cardViewDificultad.setOnClickListener {
            val intent = Intent(requireContext(), SeleccionDificultadActivity::class.java)
            startActivity(intent)
        }

        val cardViewTiempodeuso: CardView = view.findViewById(R.id.cardViewTiempodeuso)
        cardViewTiempodeuso.setOnClickListener {
            val intent = Intent(requireContext(), TiempoDeUsoActivity::class.java)
            startActivity(intent)
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

    private fun handleLogin() {
        val intent = Intent(requireContext(), InicioSesionActivity::class.java)
        startActivity(intent)
    }

    private fun handleLogout() {
        val customView = LayoutInflater.from(requireContext()).inflate(R.layout.custom_alert_dialog, null)
        val alertDialog = AlertDialog.Builder(requireContext())
            .setView(customView)
            .create()
        alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        customView.findViewById<Button>(R.id.cancelButton).setOnClickListener {
            alertDialog.dismiss()
        }
        customView.findViewById<Button>(R.id.confirmButton).setOnClickListener {
            auth.signOut()
            val intent = Intent(requireContext(), InicioSesionActivity::class.java)
            startActivity(intent)
            activity?.finish()
            alertDialog.dismiss()
        }
        alertDialog.show()
    }



    private fun cargarDificultad() {
        val sharedPref = activity?.getSharedPreferences("user_data", android.content.Context.MODE_PRIVATE) // Usar el mismo archivo de preferencias
        val dificultad = sharedPref?.getString("dificultad", null) // Obtener la dificultad guardada

        if (!dificultad.isNullOrEmpty()) {
            textViewdificultad.text = "$dificultad" // Asegúrate de que textViewDificultad esté inicializado correctamente
        } else {
            textViewdificultad.text = "No asignada" // Mensaje por defecto si no hay dificultad
        }
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

            leccionesCompletasText.text = "$completedLessons lecciones"

        }
    }

    private fun calculateLevel(completedLessonCount: Int): Int {
        //  1 nivel por cada 5 lecciones completadas, máximo 10 niveles
        return (completedLessonCount / 5).coerceAtMost(10)
    }

    private fun saveUserLevel(level: Int) {
        val sharedPref = activity?.getSharedPreferences("mis_preferencias", android.content.Context.MODE_PRIVATE)
        sharedPref?.edit()?.putInt("user_nivel", level)?.apply()
    }

    // Modificar updateProgress para actualizar en porcentaje
    private fun updateProgress(nivel: Int) {
        val progress = (nivel * 10).coerceAtMost(100) // Ajuste para reflejar el nivel de 0 a 10
        circularProgressIndicator.setProgressCompat(progress, true)
        progressText.text = "$nivel"
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
}
