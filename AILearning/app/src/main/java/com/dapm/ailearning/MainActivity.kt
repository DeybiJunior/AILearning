package com.dapm.ailearning

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.RecognitionListener
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_inicio -> {
                    loadFragment(InicioFragment())
                    true
                }
                R.id.nav_aprende -> {
                    loadFragment(AprendeFragment())
                    true
                }
                R.id.nav_perfil -> {
                    loadFragment(PerfilFragment())
                    true
                }
                else -> false
            }
        }

        // Load the default fragment
        if (savedInstanceState == null) {
            bottomNavigationView.selectedItemId = R.id.nav_inicio
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}