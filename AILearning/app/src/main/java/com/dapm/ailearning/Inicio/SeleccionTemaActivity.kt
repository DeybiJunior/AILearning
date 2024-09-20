package com.dapm.ailearning.Inicio

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dapm.ailearning.R
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SeleccionTemaActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seleccion_tema)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val autoCompleteTextView = findViewById<AutoCompleteTextView>(R.id.autoCompleteTextView)
        val temaEspecificoEditText = findViewById<TextInputEditText>(R.id.temaEspecificoText)
        val btnGuardar = findViewById<Button>(R.id.btnGuardar)
        val btnBack = findViewById<ImageButton>(R.id.btnBack)

        setupAutoCompleteTextView(autoCompleteTextView, temaEspecificoEditText)

        btnGuardar.setOnClickListener {
            val temaSeleccionado = autoCompleteTextView.text.toString().trim()
            val temaEspecifico = temaEspecificoEditText.text.toString().trim()

            if (temaSeleccionado.isEmpty() && temaEspecifico.isEmpty()) {
                showToast("Por favor selecciona un tema")
            } else {
                saveTema(temaSeleccionado, temaEspecifico)
            }
        }

        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun setupAutoCompleteTextView(autoCompleteTextView: AutoCompleteTextView, temaEspecificoEditText: TextInputEditText) {
        autoCompleteTextView.setAdapter(
            ArrayAdapter(
                this,
                android.R.layout.simple_dropdown_item_1line,
                resources.getStringArray(R.array.simple_items)
            )
        )

        autoCompleteTextView.setOnItemClickListener { _, _, _, _ ->
            temaEspecificoEditText.text?.clear()
        }

        temaEspecificoEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                autoCompleteTextView.text?.clear()
            }
        }
    }

    private fun saveTema(temaSeleccionado: String, temaEspecifico: String) {
        val temaEstudio = if (temaSeleccionado.isNotEmpty()) {
            temaSeleccionado
        } else {
            temaEspecifico
        }

        val userId = auth.currentUser?.uid
        if (userId != null) {
            val temaData = hashMapOf("temaEstudio" to temaEstudio)

            firestore.collection("users")
                .document(userId)
                .collection("lecciones")
                .document("leccionId")
                .set(temaData)
                .addOnSuccessListener {
                    showToast("Tema guardado exitosamente")
                    finish()
                }
                .addOnFailureListener { e ->
                    showToast("Error al guardar el tema: $e")
                }
        } else {
            showToast("Usuario no autenticado")
        }
    }


    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}