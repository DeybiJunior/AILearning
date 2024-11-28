package com.dapm.ailearning

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.dapm.ailearning.Aprende.AprendeFragment
import com.dapm.ailearning.Inicio.InicioFragment
import com.dapm.ailearning.Perfil.PerfilFragment

class ViewPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    private val fragmentList = listOf(
        InicioFragment(),
        AprendeFragment(),
        PerfilFragment()
    )

    override fun getItemCount(): Int = fragmentList.size

    override fun createFragment(position: Int): Fragment = fragmentList[position]
}
