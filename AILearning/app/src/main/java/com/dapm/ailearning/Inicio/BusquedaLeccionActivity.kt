package com.dapm.ailearning.Inicio

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dapm.ailearning.Datos.Leccion
import com.dapm.ailearning.R

class BusquedaLeccionActivity : AppCompatActivity() {

    private lateinit var recyclerViewAgregadas: RecyclerView
    private lateinit var recyclerViewDisponibles: RecyclerView

    private lateinit var adapterAgregadas: LeccionAdapter
    private lateinit var adapterDisponibles: LeccionAdapter

    private val lecciones: MutableList<Leccion> = mutableListOf(

    )

    private val leccionesAgregadas: MutableList<Leccion> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_busqueda_leccion)

        recyclerViewAgregadas = findViewById(R.id.recyclerViewAgregadas)
        recyclerViewDisponibles = findViewById(R.id.recyclerViewDisponibles)

        // Configurar los RecyclerViews
        recyclerViewAgregadas.layoutManager = LinearLayoutManager(this)
        recyclerViewDisponibles.layoutManager = LinearLayoutManager(this)

        adapterAgregadas = LeccionAdapter(leccionesAgregadas, leccionesAgregadas,
            onLeccionAdded = { leccion ->
                leccionesAgregadas.add(leccion)
                adapterAgregadas.notifyDataSetChanged()
            },
            onLeccionRemoved = { leccion ->
                leccionesAgregadas.remove(leccion)
                adapterAgregadas.notifyDataSetChanged()
            }
        )

        adapterDisponibles = LeccionAdapter(lecciones, leccionesAgregadas,
            onLeccionAdded = { leccion ->
                leccionesAgregadas.add(leccion)
                adapterDisponibles.notifyDataSetChanged()
            },
            onLeccionRemoved = { leccion ->
                leccionesAgregadas.remove(leccion)
                adapterDisponibles.notifyDataSetChanged()
            }
        )

        recyclerViewAgregadas.adapter = adapterAgregadas
        recyclerViewDisponibles.adapter = adapterDisponibles
    }
}
