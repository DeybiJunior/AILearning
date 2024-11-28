package com.dapm.ailearning

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        val viewPager = findViewById<ViewPager2>(R.id.view_pager)

        // Configurar el adaptador para el ViewPager2
        val adapter = ViewPagerAdapter(this)
        viewPager.adapter = adapter

        // Sincronizar ViewPager2 con BottomNavigationView
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_inicio -> {
                    viewPager.currentItem = 0
                    true
                }
                R.id.nav_aprende -> {
                    viewPager.currentItem = 1
                    true
                }
                R.id.nav_perfil -> {
                    viewPager.currentItem = 2
                    true
                }
                else -> false
            }
        }

        // Sincronizar cambios en el ViewPager2 con BottomNavigationView e íconos
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> {
                        updateIcons(bottomNavigationView, R.id.nav_inicio, R.drawable.icon_inicio)
                        bottomNavigationView.selectedItemId = R.id.nav_inicio
                    }
                    1 -> {
                        updateIcons(bottomNavigationView, R.id.nav_aprende, R.drawable.icon_aprendee)
                        bottomNavigationView.selectedItemId = R.id.nav_aprende
                    }
                    2 -> {
                        updateIcons(bottomNavigationView, R.id.nav_perfil, R.drawable.icon_perfil)
                        bottomNavigationView.selectedItemId = R.id.nav_perfil
                    }
                }
            }
        })
    }

    // Actualiza los íconos del BottomNavigationView
    private fun updateIcons(bottomNavigationView: BottomNavigationView, selectedItemId: Int, selectedIcon: Int) {
        // Restaurar los íconos predeterminados
        bottomNavigationView.menu.findItem(R.id.nav_inicio).setIcon(R.drawable.outline_home_24)
        bottomNavigationView.menu.findItem(R.id.nav_aprende).setIcon(R.drawable.outline_book_24)
        bottomNavigationView.menu.findItem(R.id.nav_perfil).setIcon(R.drawable.outline_person_24)

        // Establecer el ícono del elemento seleccionado
        bottomNavigationView.menu.findItem(selectedItemId).setIcon(selectedIcon)
    }
}
