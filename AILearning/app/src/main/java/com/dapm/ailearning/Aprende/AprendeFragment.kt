package com.dapm.ailearning.Aprende

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import com.dapm.ailearning.R
import com.dapm.ailearning.SolicituLocal.ApiService

class AprendeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_aprende, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnContinuar1 = view.findViewById<Button>(R.id.btnContinuar1)
        val btnContinuar2 = view.findViewById<Button>(R.id.btnContinuar2)
        val btnContinuar3 = view.findViewById<Button>(R.id.btnContinuar3)

        // Configurar el botón Continuar
        btnContinuar1.setOnClickListener {
            // Regresa al Aprende_Speack
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, Aprende_speack())
                .addToBackStack(null)  // Opcional: Agrega la transacción a la pila de retroceso
                .commit()
        }
        btnContinuar2.setOnClickListener {
            // Deshabilitar el botón para evitar múltiples clics mientras se espera la respuesta
            btnContinuar2.isEnabled = false

            // Llamar a la API usando requireContext()
            ApiService.solicitarAPI("Mi mascota favorita", requireContext()) { error ->
                // Volver a habilitar el botón en caso de error
                btnContinuar2.isEnabled = true

                // Manejo del error
                }
        }



        btnContinuar3.setOnClickListener {
            // Regresa al Aprende_Speack
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, Aprende_speack())
                .addToBackStack(null)  // Opcional: Agrega la transacción a la pila de retroceso
                .commit()
        }


    }
}
