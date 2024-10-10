package com.dapm.ailearning.Datos

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LeccionViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: LeccionRepository
    private val _leccionesPorUsuario = MutableLiveData<List<Leccion>>()
    val leccionesPorUsuario: LiveData<List<Leccion>> get() = _leccionesPorUsuario

    init {
        val leccionDao = AppDatabase.getDatabase(application).leccionDao()
        repository = LeccionRepository(leccionDao)
    }

    // Función para insertar una lección
    fun insert(leccion: Leccion) = viewModelScope.launch(Dispatchers.IO) {
        repository.insert(leccion)
    }

    // Función para cargar lecciones de un usuario y actualizar el LiveData
    fun getLeccionesByUserId(userId: String) = viewModelScope.launch(Dispatchers.IO) {
        val lecciones = repository.getLeccionesByUserId(userId)
        _leccionesPorUsuario.postValue(lecciones)  // Actualiza el LiveData
    }

    // Función para eliminar una lección
    fun delete(leccion: Leccion) = viewModelScope.launch(Dispatchers.IO) {
        repository.delete(leccion)
    }
}