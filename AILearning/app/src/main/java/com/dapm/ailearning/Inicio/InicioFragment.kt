package com.dapm.ailearning.Inicio

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.dapm.ailearning.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class InicioFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var txtNombreUsuario: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_inicio, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        txtNombreUsuario = view.findViewById(R.id.txtNombreUsuario)

        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            firestore.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val nombres = document.getString("nombres") ?: "Usuario"
                        txtNombreUsuario.text = nombres
                    } else {
                        txtNombreUsuario.text = "Usuario"
                    }
                }
                .addOnFailureListener { e ->
                    txtNombreUsuario.text = "Error al cargar nombre"
                }
        } else {
            txtNombreUsuario.text = "Usuario"
        }
    }
}
