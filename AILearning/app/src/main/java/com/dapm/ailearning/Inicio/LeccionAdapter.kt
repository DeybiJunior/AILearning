package com.dapm.ailearning.Inicio

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dapm.ailearning.Datos.Leccion
import com.dapm.ailearning.R

class LeccionAdapter(
    private val lecciones: MutableList<Leccion>,
    private val leccionesAgregadas: MutableList<Leccion>,
    private val onLeccionAdded: (Leccion) -> Unit,
    private val onLeccionRemoved: (Leccion) -> Unit
) : RecyclerView.Adapter<LeccionAdapter.LeccionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeccionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_leccion, parent, false)
        return LeccionViewHolder(view)
    }

    override fun onBindViewHolder(holder: LeccionViewHolder, position: Int) {
        val leccion = lecciones[position]
        holder.bind(leccion)

        // Configura la visibilidad de los botones según si la lección ya está agregada
        if (leccionesAgregadas.contains(leccion)) {
            holder.btnAgregar.visibility = View.GONE
            holder.btnQuitar.visibility = View.VISIBLE
        } else {
            holder.btnAgregar.visibility = View.VISIBLE
            holder.btnQuitar.visibility = View.GONE
        }

        holder.btnAgregar.setOnClickListener {
            onLeccionAdded(leccion) // Notificar a la actividad que se ha agregado una lección
            notifyItemRemoved(position) // Eliminar la lección de la lista de disponibles
            lecciones.removeAt(position)
            notifyDataSetChanged() // Actualizar el adaptador
        }

        holder.btnQuitar.setOnClickListener {
            onLeccionRemoved(leccion) // Notificar a la actividad que se ha quitado una lección
            leccionesAgregadas.remove(leccion)
            lecciones.add(leccion) // Agregar la lección a la lista de disponibles
            notifyDataSetChanged() // Actualizar el adaptador
        }
    }

    override fun getItemCount(): Int {
        return lecciones.size
    }

    inner class LeccionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewNombre: TextView = itemView.findViewById(R.id.textViewNombre)
        val btnAgregar: Button = itemView.findViewById(R.id.btnAgregar)
        val btnQuitar: Button = itemView.findViewById(R.id.btnQuitar)

        fun bind(leccion: Leccion) {
        }
    }
}
