package com.dapm.ailearning.Login

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.dapm.ailearning.Datos.AppDatabase
import com.dapm.ailearning.Datos.Leccion
import com.dapm.ailearning.Datos.LeccionDao
import com.dapm.ailearning.R
import kotlinx.coroutines.launch


class TuActividad : AppCompatActivity() {

    private lateinit var leccionDao: LeccionDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tu_layout)

        // Inicializa la base de datos
        val db = AppDatabase.getDatabase(applicationContext)
        leccionDao = db.leccionDao()

        // Configura el botón
        val btnGuardarLeccion = findViewById<Button>(R.id.btnGuardarLeccion)
        btnGuardarLeccion.setOnClickListener {
            guardarLeccion()
        }
    }

    private fun guardarLeccion() {
        // Recoge los datos que deseas guardar. Puedes obtenerlos de EditText, etc.
        val userId = "sdwethj"  // Cambia esto por el ID del usuario
        val tipo = "dsfg"  // Cambia esto por el tipo de lección
        val dificultad = "disdfghficil"  // Cambia esto según corresponda
        val tema = "sdfgh"  // Cambia esto por el tema
        val json = "{\"key\": \"valsdfghjhgfdsadfghjmhgfdefghnjhgfdefghjmnhgfdefgnhgfdefgnhjmue\"}"  // Cambia esto por el JSON real que desees guardar

        // Crea un objeto Leccion
        val leccion = Leccion(
            userId = userId,
            tipo = tipo,
            dificultad = dificultad,
            tema = tema,
            json = json
        )

        // Guarda la lección en la base de datos en un hilo de fondo
        lifecycleScope.launch {
            try {
                leccionDao.insert(leccion)
                // Registra los detalles de la lección guardada
                Log.d("GuardarLeccion", "Lección guardada: $leccion")

                // Opcional: Muestra un mensaje de éxito
                Toast.makeText(this@TuActividad, "Lección guardada", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e("GuardarLeccion", "Error al guardar la lección: ${e.message}")
                Toast.makeText(this@TuActividad, "Error al guardar la lección", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
