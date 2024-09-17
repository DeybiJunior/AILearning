package com.dapm.ailearning.Perfil

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.dapm.ailearning.Login.InicioSesionActivity
import com.dapm.ailearning.R
import com.google.firebase.auth.FirebaseAuth

class PerfilFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var loginButton: Button
    private lateinit var logoutButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        auth = FirebaseAuth.getInstance()

        val view = inflater.inflate(R.layout.fragment_perfil, container, false)

        loginButton = view.findViewById(R.id.loginButton)
        logoutButton = view.findViewById(R.id.logoutButton)

        val currentUser = auth.currentUser
        if (currentUser == null) {
            loginButton.visibility = View.VISIBLE
            loginButton.setOnClickListener {
                // Redirigir al usuario a la pantalla de inicio de sesión
                val intent = Intent(requireContext(), InicioSesionActivity::class.java)
                startActivity(intent)
            }
        } else {
            loginButton.visibility = View.GONE
            logoutButton.visibility = View.VISIBLE
            logoutButton.setOnClickListener {
                handleLogout()
            }
        }

        return view
    }

    private fun handleLogout() {
        auth.signOut()
        val intent = Intent(requireContext(), InicioSesionActivity::class.java)
        startActivity(intent)
        activity?.finish()
    }
}
