package com.dapm.ailearning.Login

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.DialogFragment
import com.dapm.ailearning.R


class TerminosDialogFragment(private val onAccept: () -> Unit) : DialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_terminos_dialog, container, false)
    }

    override fun onStart() {
        super.onStart()
        // Establece el fondo del diálogo como transparente
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnAceptar = view.findViewById<Button>(R.id.btnAceptar)
        val btnCancelar = view.findViewById<Button>(R.id.btnCancelar)

        btnAceptar.setOnClickListener {
            onAccept()
            dismiss() // Cierra el diálogo
        }

        btnCancelar.setOnClickListener {
            dismiss() // Cierra el diálogo
        }
    }
}