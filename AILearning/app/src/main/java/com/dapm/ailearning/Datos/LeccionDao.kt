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

}