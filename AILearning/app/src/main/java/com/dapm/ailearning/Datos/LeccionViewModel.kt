package com.dapm.ailearning.Datos

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LeccionViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: LeccionRepository
    private val _leccionesPorUsuario = MutableLiveData<List<Leccion>>()
    val leccionesPorUsuario: LiveData<List<Leccion>> = _leccionesPorUsuario


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

    fun deleteLeccion(leccion: Leccion) = viewModelScope.launch(Dispatchers.IO) {
        repository.delete(leccion) // Elimina la lección en el repositorio

        // Actualiza la lista de lecciones en el LiveData
        val listaActualizada = _leccionesPorUsuario.value?.toMutableList() ?: mutableListOf()
        listaActualizada.remove(leccion)

        // Notifica el cambio en el hilo principal
        withContext(Dispatchers.Main) {
            _leccionesPorUsuario.value = listaActualizada
        }
    }
}