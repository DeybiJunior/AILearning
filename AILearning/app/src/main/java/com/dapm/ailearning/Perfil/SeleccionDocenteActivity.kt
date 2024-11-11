// SeleccionDocenteActivity.kt
package com.dapm.ailearning.Perfil

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dapm.ailearning.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class SeleccionDocenteActivity : AppCompatActivity() {

    private lateinit var recyclerViewDocentes: RecyclerView
    private lateinit var docentesAdapter: DocentesAdapter
    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var btnEliminarDocente: Button
    private lateinit var Docentecard: CardView
    private lateinit var tvMensaje: TextView
    private lateinit var tvDocente: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seleccion_docente)

        recyclerViewDocentes = findViewById(R.id.recyclerViewDocentes)
        recyclerViewDocentes.layoutManager = LinearLayoutManager(this)

        docentesAdapter = DocentesAdapter { docenteId ->
            guardarDocenteSeleccionado(docenteId)
        }
        recyclerViewDocentes.adapter = docentesAdapter
        tvMensaje = findViewById(R.id.tvMensaje)
        tvDocente = findViewById(R.id.TvDocente)

        btnEliminarDocente = findViewById(R.id.btnEliminarDocente)
        btnEliminarDocente.setOnClickListener {
            mostrarCuadroAdvertencia()
        }

        // Check if there is an assigned teacher
        checkAssignedDocente()

        val closeButton: ImageView = findViewById(R.id.btnBack)
        closeButton.setOnClickListener {
            finish()
        }
    }

    private fun checkAssignedDocente() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId == null) {
            tvMensaje.text = "Usuario no autenticado"
            tvMensaje.visibility = View.VISIBLE
            tvDocente.visibility = View.GONE
            return
        }

        firestore.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists() && document.contains("docente_id")) {
                    val docenteId = document.getString("docente_id")
                    if (docenteId.isNullOrEmpty()) {
                        cargarDocentes() // Load teachers
                        btnEliminarDocente.visibility = Button.GONE // Hide the button
                    } else {
                        tvMensaje.visibility = View.GONE
                        tvDocente.visibility = View.GONE
                        mostrarDatosDocente(docenteId)
                        btnEliminarDocente.visibility = Button.VISIBLE // Show delete button
                    }
                } else {
                    tvDocente.text = "Selección Docente"
                    tvDocente.visibility = View.VISIBLE
                    Toast.makeText(this, "No tiene docente asignado", Toast.LENGTH_SHORT).show()
                    cargarDocentes() // Load teachers
                    btnEliminarDocente.visibility = Button.GONE // Hide the button
                }
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreError", "Error al obtener el documento: ${e.message}")
                Toast.makeText(this, "Error al obtener información del usuario", Toast.LENGTH_SHORT).show()
            }
    }

    private fun mostrarDatosDocente(docenteId: String) {
        // Primero, asegurarse de que la CardView esté oculta al iniciar la carga de datos
        val docenteCard = findViewById<CardView>(R.id.Docentecard)
        docenteCard.visibility = View.GONE // Ocultar inicialmente

        firestore.collection("users_docentes").document(docenteId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val nombre = document.getString("nombre") ?: "Nombre no disponible"
                    val apellido = document.getString("apellido") ?: "Apellido no disponible"
                    val colegio = document.getString("colegio") ?: "Colegio no disponible"
                    val email = document.getString("email") ?: "Email no disponible"

                    // Configura el texto de cada TextView en la CardView
                    findViewById<TextView>(R.id.textViewNombre).text = "$nombre $apellido" // Combina nombre y apellido
                    findViewById<TextView>(R.id.textViewColegio).text = "     Colegio: $colegio"
                    findViewById<TextView>(R.id.textViewEmail).text = "     Email: $email"

                    // Mostrar el botón de eliminar docente y la CardView
                    findViewById<Button>(R.id.btnEliminarDocente).visibility = View.VISIBLE
                    docenteCard.visibility = View.VISIBLE // Mostrar la CardView
                } else {
                    Toast.makeText(this, "Docente no encontrado", Toast.LENGTH_SHORT).show()
                    docenteCard.visibility = View.GONE // Ocultar si no se encuentra el docente
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar datos del docente", Toast.LENGTH_SHORT).show()
                docenteCard.visibility = View.GONE // Ocultar en caso de error
            }
    }

    private fun mostrarCuadroAdvertencia() {
        val builder = AlertDialog.Builder(this)
        val customView = LayoutInflater.from(this).inflate(R.layout.dialog_custom_layout, null)
        val alertDialog = builder
            .setView(customView)
            .create()
        alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        customView.findViewById<Button>(R.id.confirmButton).setOnClickListener {
            eliminarDocenteSeleccionado()
            alertDialog.dismiss()
        }
        customView.findViewById<Button>(R.id.cancelButton).setOnClickListener {
            alertDialog.dismiss()
        }
        alertDialog.show()
    }

    private fun cargarDocentes() {
        val docenteCard = findViewById<CardView>(R.id.Docentecard)
        docenteCard.visibility = View.GONE

        firestore.collection("users_docentes")
            .get()
            .addOnSuccessListener { result ->
                val docentesList = result.documents.map { document ->
                    val id = document.id  // Obtener el ID del documento
                    val nombre = document.getString("nombre") ?: "Nombre no disponible"
                    val apellido = document.getString("apellido") ?: "Apellido no disponible"
                    val colegio = document.getString("colegio") ?: "Colegio no disponible"
                    Docente(id, nombre, apellido, colegio)
                }
                docentesAdapter.submitList(docentesList)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar docentes", Toast.LENGTH_SHORT).show()
            }
    }

    private fun guardarDocenteSeleccionado(docenteId: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            finish()
            onBackPressed()
            return
        }

        firestore.collection("users_docentes").document(docenteId) // Asegúrate de que la colección es correcta
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val nombre = document.getString("nombre") ?: "Nombre no disponible"
                    val apellido = document.getString("apellido") ?: "Apellido no disponible"

                    // Guardar en SharedPreferences
                    val sharedPreferences = getSharedPreferences("mis_preferencias", MODE_PRIVATE)
                    with(sharedPreferences.edit()) {
                        putString("nombre_docente", nombre)
                        putString("apellido_docente", apellido)
                        apply() // Aplica los cambios
                    }

                    // Actualizar Firestore
                    firestore.collection("users").document(userId)
                        .update("docente_id", docenteId)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Docente asignado correctamente", Toast.LENGTH_SHORT).show()
                            finish()  // Cierra el Activity después de guardar
                        }
                        .addOnFailureListener { e ->
                            Log.e("FirestoreError", "Error al asignar docente: ${e.message}")
                            Toast.makeText(this, "Error al asignar docente", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this, "Docente no encontrado", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreError", "Error al obtener datos del docente: ${e.message}")
                Toast.makeText(this, "Error al obtener datos del docente", Toast.LENGTH_SHORT).show()
            }
    }



    private fun eliminarDocenteSeleccionado() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            finish()
            onBackPressed()
            return
        }

        firestore.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists() && document.contains("docente_id")) {
                    val docenteId = document.getString("docente_id")
                    if (docenteId.isNullOrEmpty()) {
                        Toast.makeText(this, "No tiene docente asignado", Toast.LENGTH_SHORT).show()
                    } else {
                        // Proceder a eliminar el ID del docente
                        firestore.collection("users").document(userId)
                            .update("docente_id", FieldValue.delete())
                            .addOnSuccessListener {
                                // Eliminar el nombre y apellido del docente de SharedPreferences
                                val sharedPreferences = getSharedPreferences("mis_preferencias", MODE_PRIVATE)
                                with(sharedPreferences.edit()) {
                                    remove("nombre_docente")
                                    remove("apellido_docente")
                                    apply() // Aplica los cambios
                                }

                                Toast.makeText(this, "Docente eliminado correctamente", Toast.LENGTH_SHORT).show()
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Log.e("FirestoreError", "Error al eliminar docente: ${e.message}")
                                Toast.makeText(this, "Error al eliminar docente", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    Toast.makeText(this, "No tiene docente asignado", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreError", "Error al obtener el documento: ${e.message}")
                Toast.makeText(this, "Error al obtener información del usuario", Toast.LENGTH_SHORT).show()
            }
    }

    }


