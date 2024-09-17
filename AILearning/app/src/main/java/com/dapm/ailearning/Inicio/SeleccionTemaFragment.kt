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

class SeleccionTemaFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_seleccion_tema, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val autoCompleteTextView = view.findViewById<AutoCompleteTextView>(R.id.autoCompleteTextView)
        val temaEspecificoEditText = view.findViewById<TextInputEditText>(R.id.temaEspecificoText)
        val btnGuardar = view.findViewById<Button>(R.id.btnGuardar)
        val btnBack = view.findViewById<ImageButton>(R.id.btnBack)

        // Configura el AutoCompleteTextView
        autoCompleteTextView.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, resources.getStringArray(R.array.simple_items)))

        // Configura el botón Guardar
        btnGuardar.setOnClickListener {
            if (autoCompleteTextView.text.isNullOrEmpty() && temaEspecificoEditText.text.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "Por favor, ingresa un tema", Toast.LENGTH_SHORT).show()
            } else {
                // Lógica para guardar datos
                Toast.makeText(requireContext(), "Datos guardados", Toast.LENGTH_SHORT).show()

                // Regresa al InicioFragment
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, InicioFragment())
                    .addToBackStack(null)  // Opcional: Agrega la transacción a la pila de retroceso
                    .commit()
            }
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