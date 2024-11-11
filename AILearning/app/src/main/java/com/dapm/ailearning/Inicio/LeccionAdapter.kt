package com.dapm.ailearning.Inicio

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dapm.ailearning.Datos.Leccion
import com.dapm.ailearning.R
class LeccionAdapter(
    private val onCardClick: (Leccion) -> Unit,  // Función para el clic en el CardView
    private val onDelete: (Leccion) -> Unit // Función para eliminar
) : ListAdapter<Leccion, LeccionAdapter.LeccionViewHolder>(LeccionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeccionViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_leccion, parent, false)
        return LeccionViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: LeccionViewHolder, position: Int) {
        val currentLeccion = getItem(position)
        holder.bind(currentLeccion, onCardClick, onDelete)
    }

    class LeccionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardViewLeccion: CardView = itemView.findViewById(R.id.cardViewLeccion) // CardView
        private val textViewTema: TextView = itemView.findViewById(R.id.textViewTema)
        private val textViewTipo: TextView = itemView.findViewById(R.id.textViewTipo)
        private val textViewDificultad: TextView = itemView.findViewById(R.id.textViewDificultad)
        private val textViewEstado: TextView = itemView.findViewById(R.id.textViewEstado)
        private val textViewPuntaje: TextView = itemView.findViewById(R.id.textViewPuntaje)
        private val imageButtonEliminar: ImageButton = itemView.findViewById(R.id.imageButtonEliminar)
        private val imageView: ImageView = itemView.findViewById(R.id.imageView)

        fun bind(leccion: Leccion, onCardClick: (Leccion) -> Unit, onDelete: (Leccion) -> Unit) {
            textViewTema.text = leccion.tema
            textViewTipo.text = "${leccion.tipo}"
            textViewDificultad.text = "Dificultad de la lección: ${leccion.dificultad}"
            textViewEstado.text = "Estado de la lección: ${if (leccion.estado) "Completada" else "Pendiente"}"
            textViewPuntaje.text = "Puntaje: ${leccion.puntaje} / 10"
            val imagenRes = when (leccion.tipo) {
                "Pronunciación Perfecta" -> R.drawable.pronunciacion
                "Desafío de Comprensión" -> R.drawable.comprension
                "Escucha Activa" -> R.drawable.escucha
                "Frases en Acción" -> R.drawable.frase
                "Desafío de Cartas" -> R.drawable.carta
                "Adivina la Palabra" -> R.drawable.palabra
                else -> R.drawable.otros  // Imagen por defecto si no coincide con ningún tipo
            }
            imageView.setImageResource(imagenRes)
            // Configura el clic en el CardView
            cardViewLeccion.setOnClickListener {
                onCardClick(leccion) // Llama a la función de clic en el CardView
            }

            // Configura el clic en el botón de eliminar
            imageButtonEliminar.setOnClickListener {
                onDelete(leccion) // Llama a la función de eliminar
            }
        }
    }


    class LeccionDiffCallback : DiffUtil.ItemCallback<Leccion>() {
        override fun areItemsTheSame(oldItem: Leccion, newItem: Leccion): Boolean {
            return oldItem.lessonId == newItem.lessonId
        }

        override fun areContentsTheSame(oldItem: Leccion, newItem: Leccion): Boolean {
            return oldItem == newItem
        }
    }
}