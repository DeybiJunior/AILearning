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
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.dapm.ailearning.Login.InicioSesionActivity
import com.dapm.ailearning.R
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.firebase.auth.FirebaseAuth
import kotlin.properties.Delegates

class PerfilFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var loginCard: CardView
    private lateinit var logoutCard: CardView

    private lateinit var circularProgressIndicator: CircularProgressIndicator
    private lateinit var progressText: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        circularProgressIndicator = view.findViewById(R.id.circularProgressIndicator)
        progressText = view.findViewById(R.id.progressText)

        // Recuperar el nivel del usuario desde SharedPreferences
        val sharedPref = activity?.getSharedPreferences("user_data", android.content.Context.MODE_PRIVATE)
        val nivel = sharedPref?.getInt("user_nivel", 0) ?: 0 // Obtén el nivel o 0 si no existe

        // Actualiza el progreso con el nivel recuperado
        updateProgress(nivel)

        // Llama a increaseProgress() para iniciar el progreso automáticamente, pasando el nivel recuperado
        increaseProgress(nivel) // Pasa el nivel recuperado
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
                // Redirigir al usuario a la pantalla de inicio de sesión
                val intent = Intent(requireContext(), InicioSesionActivity::class.java)
                startActivity(intent)
            }
        } else {
            loginCard.visibility = View.GONE
            logoutCard.visibility = View.VISIBLE
            logoutCard.setOnClickListener {
                handleLogout()
            }
        }

        circularProgressIndicator = view.findViewById(R.id.circularProgressIndicator)
        progressText = view.findViewById(R.id.progressText)

        // Establece el progreso inicial
        updateProgress(0)

        return view
    }

    // Función para actualizar el progreso
    private fun updateProgress(progress: Int) {
        circularProgressIndicator.setProgress(progress)
        progressText.text = "$progress%"
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
                    updateProgress((progress.toFloat() / maxProgress * 100).toInt()) // Convertir a porcentaje
                    handler.postDelayed(this, 30) // Actualiza cada 30ms
                }
            }
        }, 500)
    }


    private fun handleLogout() {
        auth.signOut()
        val intent = Intent(requireContext(), InicioSesionActivity::class.java)
        startActivity(intent)
        activity?.finish()
    }
}
