package com.dapm.ailearning.Datos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface LeccionDao {
    @Insert
    suspend fun insert(lesson: Leccion)

    @Query("SELECT * FROM lecciones WHERE userId = :userId")
    suspend fun getLessonsByUserId(userId: String): List<Leccion>

    @Delete
    suspend fun delete(lesson: Leccion)

    @Query("SELECT json FROM lecciones WHERE lessonId = :lessonId")
    suspend fun getJsonByLessonId(lessonId: Int): String

    @Query("SELECT * FROM lecciones WHERE lessonId = :lessonId LIMIT 1")
    suspend fun getLeccionById(lessonId: Int): Leccion?

    // NUEVA consulta para actualizar una lección
    @Query("UPDATE lecciones SET estado = :estado, puntaje = :puntaje WHERE lessonId = :lessonId")
    suspend fun updateLeccion(lessonId: Int, estado: Boolean, puntaje: Int)

    @Query("SELECT COUNT(*) FROM lecciones WHERE userId = :userId AND estado = 1 AND puntaje > 5")
    suspend fun getCompletedLessonCount(userId: String): Int

    @Query("SELECT COUNT(*) FROM lecciones WHERE userId = :userId AND estado = 1 AND puntaje > 5 AND dificultad = :dificultad")
    suspend fun getCompletedLessonCount(userId: String, dificultad: String): Int

    @Query("UPDATE lecciones SET duration = :duration, completionDate = :completionDate, startTime = :startTime WHERE lessonId = :lessonId")
    suspend fun updateLessonDurationAndCompletionDate(lessonId: Int, duration: Long, completionDate: Long, startTime: Long)
}


