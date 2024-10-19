package com.dapm.ailearning.Datos

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lecciones")
data class Leccion(
    @PrimaryKey(autoGenerate = true) val lessonId: Int = 0,
    val userId: String,
    val tipo: String,
    val dificultad: String,
    val tema: String,
    val json: String
)