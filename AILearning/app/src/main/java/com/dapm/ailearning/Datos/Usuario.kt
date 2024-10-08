package com.dapm.ailearning.Datos

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Usuario(
    val id: String,
    val nombres: String,
    val apellidos: String,
    val edad: Int,
    val nivel: Int
) : Parcelable
