package com.dapm.ailearning.Inicio

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.dapm.ailearning.R

class InicioFragment : Fragment() {
    private lateinit var txtNombreUsuario: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_inicio, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializar los botones
        val btnOpcion1 = view.findViewById<Button>(R.id.btnOpcion1)
        val btnOpcion2 = view.findViewById<Button>(R.id.btnOpcion2)

        // Navegación a SeleccionTemaActivity
        btnOpcion1.setOnClickListener {
            activity?.let { context ->
                val intent = Intent(context, SeleccionTemaActivity::class.java)
                startActivity(intent)
            }
        }

        // Navegación a BusquedaLeccionActivity
        btnOpcion2.setOnClickListener {
            val intent = Intent(activity, HistorialLeccionesActivity::class.java)
            startActivity(intent)
        }

        txtNombreUsuario = view.findViewById(R.id.txtNombreUsuario)

        // Recuperar o crear usuario
        val sharedPref = activity?.getSharedPreferences("user_data", android.content.Context.MODE_PRIVATE)
        var nombreUsuario = sharedPref?.getString("user_nombres", null)

        if (nombreUsuario == null) {
            // Si no hay un usuario guardado, crear un usuario offline
            guardarUsuarioOfflineEnSharedPreferences()
            nombreUsuario = sharedPref?.getString("user_nombres", "Usuario Offline")
        }

        Log.d("InicioFragment", "Nombre del usuario recuperado: $nombreUsuario")
        txtNombreUsuario.text = nombreUsuario

        // Obtener el tema de estudio
        val sharedTema = activity?.getSharedPreferences("tema", android.content.Context.MODE_PRIVATE)
        val temaEstudio = sharedTema?.getString("temaEstudio", "")
        Log.d("InicioFragment", "tema: $temaEstudio")
    }

    // Función para guardar usuario offline
    private fun guardarUsuarioOfflineEnSharedPreferences() {
        val sharedPreferences = activity?.getSharedPreferences("user_data", android.content.Context.MODE_PRIVATE)
        val editor = sharedPreferences?.edit()

        // Crear un usuario "offline" con datos predefinidos o generados
        val userIdOffline = "offline_user"
        val nombresOffline = "Usuario"
        val apellidosOffline = "Offline"
        val edadOffline = 0 // Edad genérica
        val nivelOffline = 0 // Nivel básico por defecto

        editor?.putString("user_id", userIdOffline)
        editor?.putString("user_nombres", nombresOffline)
        editor?.putString("user_apellidos", apellidosOffline)
        editor?.putInt("user_edad", edadOffline)
        editor?.putInt("user_nivel", nivelOffline)
        editor?.apply()

        Log.d("InicioFragment", "Guardando datos de usuario offline: " +
                "ID: $userIdOffline, " +
                "Nombres: $nombresOffline, " +
                "Apellidos: $apellidosOffline, " +
                "Edad: $edadOffline, " +
                "Nivel: $nivelOffline")
    }
}
