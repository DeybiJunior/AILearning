package com.dapm.ailearning.Inicio

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dapm.ailearning.Datos.LeccionViewModel
import com.dapm.ailearning.R

class HistorialLeccionesActivity : AppCompatActivity() {

    private lateinit var leccionViewModel: LeccionViewModel
    private lateinit var leccionAdapter: LeccionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historial_lecciones)

        // Configuración del RecyclerView y el Adapter
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewLecciones)
        leccionAdapter = LeccionAdapter()
        recyclerView.adapter = leccionAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Inicializar el ViewModel
        leccionViewModel = ViewModelProvider(this).get(LeccionViewModel::class.java)

        // Obtener el userId desde SharedPreferences
        val sharedPreferences = getSharedPreferences("user_data", MODE_PRIVATE)
        val userId = sharedPreferences.getString("user_id", null)

        if (userId != null) {
            // Cargar las lecciones del usuario
            leccionViewModel.getLeccionesByUserId(userId)

            // Observa los cambios en las lecciones del usuario
            leccionViewModel.leccionesPorUsuario.observe(this, { lecciones ->
                lecciones?.let { leccionAdapter.submitList(it) }
            })
        } else {
            Log.e("BusquedaLeccionActivity", "El userId es nulo, no se pueden cargar lecciones.")
        }

        // Configurar el botón de regresar
        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish()
        }
    }
}