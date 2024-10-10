package com.dapm.ailearning.Inicio

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dapm.ailearning.Datos.Leccion
import com.dapm.ailearning.R

class LeccionAdapter : ListAdapter<Leccion, LeccionAdapter.LeccionViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeccionViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_leccion, parent, false)
        return LeccionViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: LeccionViewHolder, position: Int) {
        val currentLeccion = getItem(position)
        holder.bind(currentLeccion)
    }

    class LeccionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(leccion: Leccion) {
            // Vincula los datos de la lección a los views del layout
            itemView.findViewById<TextView>(R.id.tvTema).text = leccion.tema
            itemView.findViewById<TextView>(R.id.tvDificultad).text = leccion.dificultad
            itemView.findViewById<TextView>(R.id.tvTipo).text = leccion.tipo
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Leccion>() {
            override fun areItemsTheSame(oldItem: Leccion, newItem: Leccion): Boolean {
                return oldItem.lessonId == newItem.lessonId
            }

            override fun areContentsTheSame(oldItem: Leccion, newItem: Leccion): Boolean {
                return oldItem == newItem
            }
        }
    }
}
