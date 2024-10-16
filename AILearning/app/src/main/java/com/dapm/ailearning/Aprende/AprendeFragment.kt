package com.dapm.ailearning.Aprende

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.dapm.ailearning.Datos.LeccionViewModel
import com.dapm.ailearning.R

class AprendeFragment : Fragment() {

    private lateinit var leccionViewModel: LeccionViewModel
    private lateinit var linearLayout: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_aprende, container, false)

        // Encuentra el LinearLayout donde se agregarán las cards
        linearLayout = view.findViewById(R.id.linearLayoutContainer)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializar ViewModel
        leccionViewModel = ViewModelProvider(this).get(LeccionViewModel::class.java)

        // Obtener el userId desde SharedPreferences
        val sharedPreferences = requireActivity().getSharedPreferences("user_data", AppCompatActivity.MODE_PRIVATE)
        val userId = sharedPreferences.getString("user_id", null)

        if (userId != null) {
            // Cargar las lecciones del usuario
            leccionViewModel.getLeccionesByUserId(userId)

            // Observar los cambios en las lecciones
            leccionViewModel.leccionesPorUsuario.observe(viewLifecycleOwner, { lecciones ->
                lecciones?.let {
                    // Ordenar las lecciones por lessonId de manera descendente
                    val leccionesOrdenadas = it.sortedByDescending { leccion -> leccion.lessonId }

                    // Crear una card para cada lección en el orden descendente
                    for (leccion in leccionesOrdenadas) {
                        crearCardLeccion(leccion.tipo, leccion.dificultad, leccion.lessonId)
                    }
                }
            })
        }
    }

    private fun crearCardLeccion(nombre: String, nivel: String, lessonId: Int) {
        // Inflar el diseño del CardView
        val cardView = layoutInflater.inflate(R.layout.item_leccion_boton, linearLayout, false)

        // Encontrar los elementos del card inflado
        val nombreTextView: TextView = cardView.findViewById(R.id.tvLeccionNombre)
        val nivelTextView: TextView = cardView.findViewById(R.id.tvLeccionNivel)
        val button: Button = cardView.findViewById(R.id.btnContinuar)

        // Asignar los valores a los elementos
        nombreTextView.text = nombre
        nivelTextView.text = "Nivel: $nivel"

        // Configurar el botón
        button.setOnClickListener {
            // Crear un Intent para iniciar la actividad Aprendeporrepeticion
            val intent = Intent(requireContext(), Aprendeporrepeticion::class.java)
            // Pasar el id de la lección a través del intent
            intent.putExtra("idLeccion", lessonId) // Asegúrate de pasar el lessonId
            startActivity(intent)
        }

        // Añadir el CardView al LinearLayout
        linearLayout.addView(cardView)
    }

}

