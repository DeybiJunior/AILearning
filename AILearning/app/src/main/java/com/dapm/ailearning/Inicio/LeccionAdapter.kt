package com.dapm.ailearning.Inicio

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dapm.ailearning.Datos.Leccion
import com.dapm.ailearning.R

class LeccionAdapter : ListAdapter<Leccion, LeccionAdapter.LeccionViewHolder>(LeccionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeccionViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_leccion, parent, false)
        return LeccionViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: LeccionViewHolder, position: Int) {
        val currentLeccion = getItem(position)
        holder.bind(currentLeccion)
    }

    class LeccionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewTema: TextView = itemView.findViewById(R.id.textViewTema)
        private val textViewDificultad: TextView = itemView.findViewById(R.id.textViewDificultad)
        private val textViewEstado: TextView = itemView.findViewById(R.id.textViewEstado)
        private val textViewPuntaje: TextView = itemView.findViewById(R.id.textViewPuntaje)

        fun bind(leccion: Leccion) {
            textViewTema.text = leccion.tema
            textViewDificultad.text = "Dificultad: ${leccion.dificultad}"
            textViewEstado.text = "Estado: ${if (leccion.estado) "Completada" else "Pendiente"}"
            textViewPuntaje.text = "Puntaje: ${leccion.puntaje}"
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
