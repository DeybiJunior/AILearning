package com.dapm.ailearning.Inicio

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dapm.ailearning.R
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
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

        // Deshabilita el botón inicialmente
        btnGuardar.isEnabled = false

        // Agrega un TextWatcher para habilitar el botón cuando ambos campos estén completos
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Habilita el botón si ambos campos están completos
                btnGuardar.isEnabled = autoCompleteTextView.text.isNotEmpty() || temaEspecificoEditText.text.toString().isNotEmpty()
            }
            override fun afterTextChanged(s: Editable?) {}
        }

        // Añade el TextWatcher a ambos EditText
        autoCompleteTextView.addTextChangedListener(textWatcher)
        temaEspecificoEditText.addTextChangedListener(textWatcher)

        setupAutoCompleteTextView(autoCompleteTextView, temaEspecificoEditText)

        btnGuardar.setOnClickListener {
            val temaSeleccionado = autoCompleteTextView.text.toString().trim()
            val temaEspecifico = temaEspecificoEditText.text.toString().trim()

            val temaEspecificoLayout = findViewById<TextInputLayout>(R.id.temaEspecifico)

            if (temaEspecifico.length > 50) {
                temaEspecificoLayout.error = getString(R.string.error_temaespecifico_longitud)
            }
            else if (!temaEspecifico.matches("^[a-zA-Z0-9 ]*$".toRegex())) {
                temaEspecificoLayout.error = getString(R.string.error_temaespecifico_formato)
            } else {
                temaEspecificoLayout.error = null
                saveTema(temaSeleccionado, temaEspecifico)
                val intent = Intent(this, BusquedaLeccionActivity::class.java)
                    startActivity(intent)
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
        // Inicializa SharedPreferences
        val sharedPreferences = getSharedPreferences("tema", Context.MODE_PRIVATE)

        // Guardar en SharedPreferences
        val editor = sharedPreferences.edit()
        editor.putString("temaEstudio", temaEstudio)
        editor.apply()

        showToast("Tema guardado exitosamente")
        Log.d("SeleccionTemaActivity", "Tema guardado: $temaEstudio")
        finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}