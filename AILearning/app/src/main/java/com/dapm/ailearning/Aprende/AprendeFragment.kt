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
import com.dapm.ailearning.Datos.Leccion
import com.dapm.ailearning.Datos.LeccionViewModel
import com.dapm.ailearning.Inicio.SeleccionTemaActivity
import com.dapm.ailearning.R
import com.google.android.material.button.MaterialButton

class AprendeFragment : Fragment() {

    private lateinit var leccionViewModel: LeccionViewModel
    private lateinit var linearLayout: LinearLayout
    private lateinit var linearNoLecciones: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_aprende, container, false)

        // Encuentra el LinearLayout donde se agregarán las cards
        linearLayout = view.findViewById(R.id.linearLayoutContainer)
        linearNoLecciones = view.findViewById(R.id.LinearNoLecciones)

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
            // Observar los cambios en las lecciones
            leccionViewModel.leccionesPorUsuario.observe(viewLifecycleOwner) { lecciones ->
                lecciones?.let {
                    actualizarLecciones(it)
                }
            }

            // Configurar el botón para redirigir a SeleccionTemaActivity
            btnSeleccionTema.setOnClickListener {
                val intent = Intent(requireContext(), SeleccionTemaActivity::class.java)
                startActivity(intent)
            }
        }
    }

    override fun onResume() {
        super.onResume()

        // Recargar las lecciones al volver al fragmento
        val sharedPreferences = requireActivity().getSharedPreferences("user_data", AppCompatActivity.MODE_PRIVATE)
        val userId = sharedPreferences.getString("user_id", null)

        if (userId != null) {
            leccionViewModel.getLeccionesByUserId(userId)
        }
    }

    private fun actualizarLecciones(lecciones: List<Leccion>) {
        // Filtrar las lecciones con estado false
        val leccionesFiltradas = lecciones.filter { leccion -> !leccion.estado }

        // Ordenar las lecciones filtradas por lessonId de manera descendente
        val leccionesOrdenadas = leccionesFiltradas.sortedByDescending { leccion -> leccion.lessonId }

        // Limpiar el LinearLayout antes de agregar nuevas lecciones
        linearLayout.removeAllViews()

        if (leccionesOrdenadas.isEmpty()) {
            // Mostrar mensaje y botón si no hay lecciones
            linearNoLecciones.visibility = View.VISIBLE
        } else {
            // Ocultar mensaje y botón si hay lecciones
            linearNoLecciones.visibility = View.GONE

            // Crear una card para cada lección en el orden descendente
            for (leccion in leccionesOrdenadas) {
                crearCardLeccion(leccion.tipo, leccion.dificultad, leccion.lessonId)
            }
        }
    }

    private fun crearCardLeccion(nombre: String, nivel: String, lessonId: Int) {
        // Inflar el diseño del CardView
        val cardView = layoutInflater.inflate(R.layout.item_leccion_boton, linearLayout, false)

        // Encontrar los elementos del card inflado
        val nombreTextView: TextView = cardView.findViewById(R.id.tvLeccionNombre)
        val nivelTextView: TextView = cardView.findViewById(R.id.tvLeccionNivel)
        val button: MaterialButton = cardView.findViewById(R.id.btnContinuar)

        // Asignar los valores a los elementos
        nombreTextView.text = nombre
        nivelTextView.text = "Nivel: $nivel"

        val activityMap = mapOf(
            "Pronunciación Perfecta" to Aprendeporrepeticion::class.java,
            "Desafío de Comprensión" to DesafioComprensionActivity::class.java,
            "Escucha Activa" to EscuchaActivaActivity::class.java,
            "Frases en Acción" to FrasesEnAccionActivity::class.java,
            "Desafío de Cartas" to DesafioCartasActivity::class.java,
            "Adivina la Palabra" to AdivinaPalabraActivity::class.java
        )

        // Map de íconos por nombre de lección
        val iconMap = mapOf(
            "Pronunciación Perfecta" to R.drawable.pronunciacion,
            "Desafío de Comprensión" to R.drawable.comprension,
            "Escucha Activa" to R.drawable.escucha,
            "Frases en Acción" to R.drawable.frase,
            "Desafío de Cartas" to R.drawable.carta,
            "Adivina la Palabra" to R.drawable.palabra
        )

        // Configurar el icono del botón según el nombre de la lección
        val iconResId = iconMap[nombre] ?: R.drawable.aprovado // Icono por defecto
        button.setIconResource(iconResId)

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
