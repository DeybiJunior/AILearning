package com.dapm.ailearning.Inicio

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
            val intent = Intent(activity, BusquedaLeccionActivity::class.java)
            startActivity(intent)
        }

        txtNombreUsuario = view.findViewById(R.id.txtNombreUsuario)

        // Recuperar el nombre del usuario desde SharedPreferences
        val sharedPref = activity?.getSharedPreferences("user_data", android.content.Context.MODE_PRIVATE)
        val nombreUsuario = sharedPref?.getString("user_nombres", "Usuario") // Usar la misma clave

        Log.d("InicioFragment", "Nombre del usuario recuperado: $nombreUsuario")
        txtNombreUsuario.text = nombreUsuario
    }
}
