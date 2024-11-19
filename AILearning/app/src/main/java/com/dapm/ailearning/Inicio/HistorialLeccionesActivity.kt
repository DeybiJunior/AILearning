package com.dapm.ailearning.Inicio

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dapm.ailearning.Aprende.AdivinaPalabraActivity
import com.dapm.ailearning.Aprende.Aprendeporrepeticion
import com.dapm.ailearning.Aprende.DesafioCartasActivity
import com.dapm.ailearning.Aprende.DesafioComprensionActivity
import com.dapm.ailearning.Aprende.EscuchaActivaActivity
import com.dapm.ailearning.Aprende.FrasesEnAccionActivity
import com.dapm.ailearning.Datos.LeccionViewModel
import com.dapm.ailearning.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HistorialLeccionesActivity : AppCompatActivity() {

    private lateinit var leccionViewModel: LeccionViewModel
    private lateinit var leccionAdapter: LeccionAdapter
    private val db = FirebaseFirestore.getInstance()  // Inicializa Firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historial_lecciones)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewLecciones)
        leccionAdapter = LeccionAdapter(
            onCardClick = { leccion ->
                val activityMap = mapOf(
                    "Pronunciación Perfecta" to Aprendeporrepeticion::class.java,
                    "Desafío de Comprensión" to DesafioComprensionActivity::class.java,
                    "Escucha Activa" to EscuchaActivaActivity::class.java,
                    "Frases en Acción" to FrasesEnAccionActivity::class.java,
                    "Desafío de Cartas" to DesafioCartasActivity::class.java,
                    "Adivina la Palabra" to AdivinaPalabraActivity::class.java
                )

                val activityClass = activityMap[leccion.tipo]
                activityClass?.let {
                    val intent = Intent(this, it)
                    intent.putExtra("idLeccion", leccion.lessonId)
                    startActivity(intent)
                } ?: run {
                    Toast.makeText(this, "No se encontró actividad para esta lección", Toast.LENGTH_SHORT).show()
                }
            },
            onDelete = { leccion ->
                // Mostrar el AlertDialog con diseño personalizado
                val dialogView = layoutInflater.inflate(R.layout.dialog_confirm_delete, null)

                val alertDialog = AlertDialog.Builder(this)
                    .setView(dialogView)
                    .create()
                alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
                val buttonCancel = dialogView.findViewById<Button>(R.id.buttonCancel)
                val buttonConfirm = dialogView.findViewById<Button>(R.id.buttonConfirm)

                buttonCancel.setOnClickListener {
                    alertDialog.dismiss()  // Cierra el cuadro de diálogo
                }



                buttonConfirm.setOnClickListener {
                    val userId = FirebaseAuth.getInstance().currentUser?.uid

                    leccionViewModel.deleteLeccion(leccion)
                    Toast.makeText(this, "Lección eliminada", Toast.LENGTH_SHORT).show()
                    alertDialog.dismiss()  // Cierra el cuadro de diálogo

                    if (userId != null) {
                        // Eliminar de Firestore
                        db.collection("users")
                            .document(userId)
                            .collection("lecciones")
                            .document(leccion.lessonId.toString())
                            .delete()
                            .addOnSuccessListener {
                                // Si se elimina correctamente de Firebase, eliminar localmente
                                leccionViewModel.deleteLeccion(leccion)
                                Toast.makeText(this, "Lección eliminada.", Toast.LENGTH_SHORT).show()
                                alertDialog.dismiss()  // Cierra el cuadro de diálogo
                            }
                            .addOnFailureListener { e ->
                                Log.e("Firebase", "Error al eliminar la lección. ${leccion.lessonId}", e)
                                Toast.makeText(this, "Error al eliminar la lección.", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        Toast.makeText(this, "Error: Usuario no autenticado.", Toast.LENGTH_SHORT).show()
                    }
                }


                alertDialog.show()
            }
            )
        recyclerView.adapter = leccionAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        leccionViewModel = ViewModelProvider(this).get(LeccionViewModel::class.java)
        val sharedPreferences = getSharedPreferences("user_data", MODE_PRIVATE)
        val userId = sharedPreferences.getString("user_id", null)

        if (userId != null) {
            leccionViewModel.getLeccionesByUserId(userId)
            leccionViewModel.leccionesPorUsuario.observe(this) { lecciones ->
                lecciones?.let {
                    val leccionesInvertidas = it.reversed()
                    leccionAdapter.submitList(leccionesInvertidas)
                }
            }

        } else {
            Log.e("HistorialLeccionesActivity", "El userId es nulo, no se pueden cargar lecciones.")
        }

        // Configurar el botón de regreso
        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish()
        }

        // Configurar el botón para enviar a Firebase
        val btnEnviarFirebase = findViewById<Button>(R.id.btnEnviarFirebase)
        btnEnviarFirebase.setOnClickListener {
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser != null) {
                val userId = currentUser.uid
                enviarLeccionesAFirebase(userId)
            } else {
                Toast.makeText(this, "Error: No has iniciado sesión", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun enviarLeccionesAFirebase(userId: String) {
        leccionViewModel.leccionesPorUsuario.value?.let { lecciones ->
            if (lecciones.isEmpty()) {
                Toast.makeText(this, "No hay lecciones para enviar.", Toast.LENGTH_SHORT).show()
                return
            }

            var leccionesSubidas = 0
            val totalLecciones = lecciones.size

            lecciones.forEach { leccion ->
                val leccionData = mapOf(
                    "idLeccion" to leccion.lessonId,
                    "tipo" to leccion.tipo,
                    "dificultad" to leccion.dificultad,
                    "tema" to leccion.tema,
                    "json" to leccion.json,
                    "estado" to leccion.estado,
                    "puntaje" to leccion.puntaje,
                    "startTime" to leccion.startTime,
                    "duration" to leccion.duration,
                    "completionDate" to leccion.completionDate
                )

                db.collection("users")
                    .document(userId)
                    .collection("lecciones")
                    .document(leccion.lessonId.toString())
                    .set(leccionData)
                    .addOnSuccessListener {
                        leccionesSubidas++
                        Log.d("Firebase", "Lección enviada exitosamente: ${leccion.lessonId}")

                        // Muestra el Toast solo cuando se han subido todas las lecciones
                        if (leccionesSubidas == totalLecciones) {
                            Toast.makeText(this, "Lecciones subidas exitosamente.", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("Firebase", "Error al enviar lección: ${leccion.lessonId}", e)
                    }
            }
        } ?: run {
            Log.e("HistorialLeccionesActivity", "No hay lecciones para enviar.")
            Toast.makeText(this, "No hay lecciones para enviar.", Toast.LENGTH_SHORT).show()
        }
    }

}

