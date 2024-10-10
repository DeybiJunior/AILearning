package com.dapm.ailearning.Datos

class LeccionRepository(private val leccionDao: LeccionDao) {

    // Función para insertar una lección
    suspend fun insert(leccion: Leccion) {
        leccionDao.insert(leccion)
    }

    // Función para obtener lecciones de un usuario
    suspend fun getLeccionesByUserId(userId: String): List<Leccion> {
        return leccionDao.getLessonsByUserId(userId)
    }

    // Función para eliminar una lección
    suspend fun delete(leccion: Leccion) {
        leccionDao.delete(leccion)
    }
}