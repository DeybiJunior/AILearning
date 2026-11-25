package com.dapm.ailearning.Perfil

import android.content.ActivityNotFoundException
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.dapm.ailearning.Datos.AppDatabase
import com.dapm.ailearning.Datos.LeccionDao
import com.dapm.ailearning.Login.InicioSesionActivity
import com.dapm.ailearning.R
import com.dapm.ailearning.SolicitudApi.GeminiApiKeyStorage
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
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
    private lateinit var textViewApiKeyStatus: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        circularProgressIndicator = view.findViewById(R.id.circularProgressIndicator)
        progressText = view.findViewById(R.id.progressText)
        textViewDocenteSeleccionado = view.findViewById(R.id.textViewDocenteSeleccionado)
        textViewdificultad = view.findViewById(R.id.textViewdificultad)
        leccionesCompletasText = view.findViewById(R.id.leccionesCompletasText)
        textViewApiKeyStatus = view.findViewById(R.id.textViewApiKeyStatus)

        val database = AppDatabase.getDatabase(requireContext())
        leccionDao = database.leccionDao()

        updateLevelBasedOnLessons()

        val nivel = getUserLevel() // Obtén el nivel o 0 si no existe
        updateProgress(nivel)
        increaseProgress(nivel) // Pasa el nivel recuperado
        actualizarEstadoApiKey()
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

        val cardViewApiKey: CardView = view.findViewById(R.id.cardViewApiKey)
        cardViewApiKey.setOnClickListener {
            mostrarDialogoApiKey()
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
            val sharedPreferences = requireContext().getSharedPreferences("user_data", MODE_PRIVATE)
            sharedPreferences.edit().clear().apply()
            Log.d("SharedPrefs", "Datos de usuario eliminados de SharedPreferences")

            val misPreferencias = requireContext().getSharedPreferences("mis_preferencias", MODE_PRIVATE)
            misPreferencias.edit().clear().apply()
            Log.d("SharedPrefs", "Datos eliminados de 'mis_preferencias'")

            // Eliminar datos de la base de datos
            lifecycleScope.launch {
                try {
                    val database = AppDatabase.getDatabase(requireContext())
                    database.leccionDao().deleteAll()
                    Log.d("RoomDatabase", "Todas las lecciones han sido eliminadas")
                } catch (e: Exception) {
                    Log.e("RoomDatabase", "Error al eliminar lecciones: ${e.message}")
                }
            }

            // Cerrar sesión de Firebase (si se usa Firebase Auth)
            auth.signOut()
            Log.d("FirebaseAuth", "El usuario ha cerrado sesión correctamente")

            // Redirigir a la actividad de inicio de sesión
            val intent = Intent(requireContext(), InicioSesionActivity::class.java)
            startActivity(intent)
            activity?.finish()

            // Cerrar el diálogo
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

    private fun actualizarEstadoApiKey() {
        textViewApiKeyStatus.text = if (GeminiApiKeyStorage.get(requireContext()) == null) {
            getString(R.string.gemini_api_key_not_configured)
        } else {
            getString(R.string.gemini_api_key_configured)
        }
    }

    private fun mostrarDialogoApiKey() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_gemini_api_key, null)
        val inputLayout = dialogView.findViewById<TextInputLayout>(R.id.apiKeyInputLayout)
        val input = dialogView.findViewById<TextInputEditText>(R.id.editTextGeminiApiKey)
        val openAiStudioButton = dialogView.findViewById<Button>(R.id.buttonOpenGeminiApiKeyPage)
        val pasteButton = dialogView.findViewById<Button>(R.id.buttonPasteGeminiApiKey)
        val savedApiKey = GeminiApiKeyStorage.get(requireContext())

        input.setText(savedApiKey.orEmpty())
        input.setSelection(input.text?.length ?: 0)
        input.doAfterTextChanged {
            inputLayout.error = null
        }

        openAiStudioButton.setOnClickListener {
            abrirGoogleAiStudio()
        }

        pasteButton.setOnClickListener {
            val clipboard = requireContext()
                .getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipboardText = clipboard.primaryClip
                ?.takeIf { it.itemCount > 0 }
                ?.getItemAt(0)
                ?.coerceToText(requireContext())
                ?.toString()
                .orEmpty()

            if (clipboardText.isBlank()) {
                Toast.makeText(
                    requireContext(),
                    R.string.gemini_api_key_clipboard_empty,
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                val apiKey = GeminiApiKeyStorage.normalize(clipboardText)
                input.setText(apiKey)
                input.setSelection(apiKey.length)
            }
        }

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.gemini_api_key_dialog_title)
            .setView(dialogView)
            .setPositiveButton(R.string.gemini_api_key_save, null)
            .setNeutralButton(R.string.gemini_api_key_delete, null)
            .setNegativeButton(R.string.gemini_api_key_cancel, null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val apiKey = input.text?.toString().orEmpty()
                when {
                    apiKey.isBlank() -> {
                        inputLayout.error = getString(R.string.gemini_api_key_required_error)
                        input.requestFocus()
                    }
                    !GeminiApiKeyStorage.hasValidFormat(apiKey) -> {
                        inputLayout.error = getString(R.string.gemini_api_key_format_error)
                        input.requestFocus()
                    }
                    else -> {
                        GeminiApiKeyStorage.save(requireContext(), apiKey)
                        actualizarEstadoApiKey()
                        Toast.makeText(
                            requireContext(),
                            R.string.gemini_api_key_saved,
                            Toast.LENGTH_SHORT
                        ).show()
                        dialog.dismiss()
                    }
                }
            }

            dialog.getButton(AlertDialog.BUTTON_NEUTRAL).apply {
                isEnabled = savedApiKey != null
                setOnClickListener {
                    GeminiApiKeyStorage.clear(requireContext())
                    actualizarEstadoApiKey()
                    Toast.makeText(
                        requireContext(),
                        R.string.gemini_api_key_deleted,
                        Toast.LENGTH_SHORT
                    ).show()
                    dialog.dismiss()
                }
            }
        }

        dialog.show()
    }

    private fun abrirGoogleAiStudio() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(GeminiApiKeyStorage.GOOGLE_AI_STUDIO_URL))
        try {
            startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(
                requireContext(),
                R.string.gemini_api_key_browser_error,
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
