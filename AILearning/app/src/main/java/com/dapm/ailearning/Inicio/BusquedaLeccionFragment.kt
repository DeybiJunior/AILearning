package com.dapm.ailearning.Inicio

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import com.dapm.ailearning.R
import com.google.android.material.textfield.TextInputEditText

class BusquedaLeccionFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_busqueda_leccion, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnGuardar = view.findViewById<Button>(R.id.btnGuardar)
        val btnBack = view.findViewById<ImageButton>(R.id.btnBack)

        // Configura el botón Guardar
        btnGuardar.setOnClickListener {
            // Regresa al InicioFragment
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, InicioFragment())
                .addToBackStack(null)  // Opcional: Agrega la transacción a la pila de retroceso
                .commit()
        }
        // Configura el botón Back
        btnBack.setOnClickListener {
            // Regresa al InicioFragment
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, InicioFragment())
                .addToBackStack(null)  // Opcional: Agrega la transacción a la pila de retroceso
                .commit()
        }
    }
}