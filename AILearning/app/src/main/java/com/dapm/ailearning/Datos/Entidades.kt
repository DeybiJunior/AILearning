package com.dapm.ailearning.Datos

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "usuarios")
data class Usuario(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    val apellido: String,
    val edad: Int
)

@Entity(tableName = "lecciones")
data class Leccion(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val usuarioId: Int,  // Relación con el usuario
    val tipo: String,
    val dificultad: String,
    val tema: String
)
