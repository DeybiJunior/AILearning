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
import com.dapm.ailearning.Inicio.SeleccionTemaActivity
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

        // Referencias a los nuevos elementos
        val tvNoLecciones: TextView = view.findViewById(R.id.tvNoLecciones)
        val btnSeleccionTema: Button = view.findViewById(R.id.btnSeleccionTema)

        if (userId != null) {
            // Cargar las lecciones del usuario
            leccionViewModel.getLeccionesByUserId(userId)

            // Observar los cambios en las lecciones
            leccionViewModel.leccionesPorUsuario.observe(viewLifecycleOwner, { lecciones ->
                lecciones?.let {
                    // Filtrar las lecciones con estado false
                    val leccionesFiltradas = it.filter { leccion -> !leccion.estado }

                    // Ordenar las lecciones filtradas por lessonId de manera descendente
                    val leccionesOrdenadas = leccionesFiltradas.sortedByDescending { leccion -> leccion.lessonId }

                    // Limpiar el LinearLayout antes de agregar nuevas lecciones
                    linearLayout.removeAllViews()

                    if (leccionesOrdenadas.isEmpty()) {
                        // Mostrar mensaje y botón si no hay lecciones
                        tvNoLecciones.visibility = View.VISIBLE
                        btnSeleccionTema.visibility = View.VISIBLE
                    } else {
                        // Ocultar mensaje y botón si hay lecciones
                        tvNoLecciones.visibility = View.GONE
                        btnSeleccionTema.visibility = View.GONE

                        // Crear una card para cada lección en el orden descendente
                        for (leccion in leccionesOrdenadas) {
                            crearCardLeccion(leccion.tipo, leccion.dificultad, leccion.lessonId)
                        }
                    }
                }
            })

            // Configurar el botón para redirigir a SeleccionTemaActivity
            btnSeleccionTema.setOnClickListener {
                val intent = Intent(requireContext(), SeleccionTemaActivity::class.java)
                startActivity(intent)
            }
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

        val activityMap = mapOf(
            "Pronunciación Perfecta" to Aprendeporrepeticion::class.java,
            "Desafío de Comprensión" to DesafioComprensionActivity::class.java,
            "Escucha Activa" to EscuchaActivaActivity::class.java,
            "Frases en Acción" to FrasesEnAccionActivity::class.java,
            "Desafío de Cartas" to DesafioCartasActivity::class.java
        )

        // Configurar el botón
        button.setOnClickListener {
            val activityClass = activityMap[nombre]

            activityClass?.let {
                val intent = Intent(requireContext(), it)
                intent.putExtra("idLeccion", lessonId)
                startActivity(intent)
            }
        }


        // Añadir el CardView al LinearLayout
        linearLayout.addView(cardView)
    }

}

