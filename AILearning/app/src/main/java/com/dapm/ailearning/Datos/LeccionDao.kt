package com.dapm.ailearning.Datos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface LeccionDao {
    @Insert
    suspend fun insert(lesson: Leccion)


    @Insert
    suspend fun insertAll(vararg lessons: Leccion)


    @Query("SELECT * FROM lecciones WHERE userId = :userId")
    suspend fun getLessonsByUserId(userId: String): List<Leccion>

    @Delete
    suspend fun delete(lesson: Leccion)

    @Query("SELECT json FROM lecciones WHERE lessonId = :lessonId")
    suspend fun getJsonByLessonId(lessonId: Int): String

    @Query("SELECT * FROM lecciones WHERE lessonId = :lessonId LIMIT 1")
    suspend fun getLeccionById(lessonId: Int): Leccion?

    @Query("UPDATE lecciones SET estado = :estado, puntaje = :puntaje WHERE lessonId = :lessonId")
    suspend fun updateLeccion(lessonId: Int, estado: Boolean, puntaje: Int)

    @Query("SELECT COUNT(*) FROM lecciones WHERE userId = :userId AND estado = 1 AND puntaje > 5")
    suspend fun getCompletedLessonCount(userId: String): Int

    @Query("SELECT COUNT(*) FROM lecciones WHERE userId = :userId AND estado = 1 AND puntaje > 5 AND dificultad = :dificultad")
    suspend fun getCompletedLessonCount(userId: String, dificultad: String): Int

    @Query("UPDATE lecciones SET duration = :duration, completionDate = :completionDate, startTime = :startTime WHERE lessonId = :lessonId")
    suspend fun updateLessonDurationAndCompletionDate(lessonId: Int, duration: Long, completionDate: Long, startTime: Long)

    // función para actualizar el atributo 'respuestasSeleccionadas' directamente con la lógica de concatenación
    @Transaction
    suspend fun agregarRespuestasSeleccionadas(lessonId: Int, nuevasRespuestas: String) {
        // Obtener las respuestas existentes
        val respuestasExistentes = getRespuestasSeleccionadas(lessonId)

        // Concatenar las respuestas correctamente
        val respuestasActualizadas = if (respuestasExistentes.isEmpty()) {
            nuevasRespuestas // Si no hay respuestas, simplemente usa las nuevas
        } else {
            // Si ya hay respuestas, concatenarlas correctamente sin coma extra al final
            if (respuestasExistentes.endsWith(",")) {
                // Si la respuesta existente ya termina con coma, eliminamos la coma y agregamos las nuevas respuestas
                "$respuestasExistentes$nuevasRespuestas"
            } else {
                // Si no termina con coma, agregamos una coma antes de concatenar
                "$respuestasExistentes,$nuevasRespuestas"
            }
        }

        // Actualizar las respuestas en la base de datos
        updateRespuestasSeleccionadas(lessonId, respuestasActualizadas)
    }

    // Obtener las respuestas seleccionadas
    @Query("SELECT respuestasSeleccionadas FROM lecciones WHERE lessonId = :lessonId")
    suspend fun getRespuestasSeleccionadas(lessonId: Int): String

    // Actualizar las respuestas seleccionadas
    @Query("UPDATE lecciones SET respuestasSeleccionadas = :respuestas WHERE lessonId = :lessonId")
    suspend fun updateRespuestasSeleccionadas(lessonId: Int, respuestas: String)
}


