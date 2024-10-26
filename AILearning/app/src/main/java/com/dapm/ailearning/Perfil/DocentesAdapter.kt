// DocentesAdapter.kt
package com.dapm.ailearning.Perfil

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dapm.ailearning.R

data class Docente(val id: String, val nombre: String, val apellido: String, val colegio: String)

class DocentesAdapter(
    private val onAgregarClick: (String) -> Unit
) : RecyclerView.Adapter<DocentesAdapter.DocenteViewHolder>() {

    private var docentesList = listOf<Docente>()  // Lista de docentes

    fun submitList(list: List<Docente>) {
        docentesList = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DocenteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_docente, parent, false)
        return DocenteViewHolder(view)
    }

    override fun onBindViewHolder(holder: DocenteViewHolder, position: Int) {
        val docente = docentesList[position]
        holder.bind(docente)
    }

    override fun getItemCount() = docentesList.size

    inner class DocenteViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val textViewDocenteNombre: TextView = view.findViewById(R.id.textViewDocenteNombre)
        private val textViewDocenteColegio: TextView = view.findViewById(R.id.textViewDocenteColegio)
        private val buttonAgregarDocente: Button = view.findViewById(R.id.buttonAgregarDocente)

        fun bind(docente: Docente) {
            textViewDocenteNombre.text = "${docente.nombre} ${docente.apellido}"
            textViewDocenteColegio.text = docente.colegio
            buttonAgregarDocente.setOnClickListener {
                onAgregarClick(docente.id)
            }
        }
    }
}
