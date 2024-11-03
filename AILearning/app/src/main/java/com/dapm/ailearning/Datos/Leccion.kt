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
    val json: String,
    val estado: Boolean,
    val puntaje: Int,
    val startTime: Long = 0,
    val duration: Long = 0,
    val completionDate: Long = 0
)