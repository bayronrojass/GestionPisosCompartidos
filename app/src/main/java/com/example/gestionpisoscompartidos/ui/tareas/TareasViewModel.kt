package com.example.gestionpisoscompartidos.ui.tareas

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestionpisoscompartidos.data.remote.NetworkModule
import com.example.gestionpisoscompartidos.data.repository.repositories.RepositoryTarea
import com.example.gestionpisoscompartidos.model.Tarea
import com.example.gestionpisoscompartidos.model.TareaRequest
import kotlinx.coroutines.launch

class TareasViewModel(
    private val casaId: Long,
) : ViewModel() {
    private val repository = RepositoryTarea(NetworkModule.tareaApiService)

    private val _tareas = MutableLiveData<List<Tarea>>()
    val tareas: LiveData<List<Tarea>> = _tareas

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init {
        cargarTareas()
    }

    fun cargarTareas() {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                _tareas.value = repository.getTareasByCasaId(casaId)
            } catch (e: Exception) {
                Log.e("TareasViewModel", "Error en cargarTareas", e)
                _error.value = e.message ?: "Error cargando tareas"
                _tareas.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun crearTarea(
        nombre: String,
        descripcion: String?,
    ) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val request = TareaRequest(nombre, descripcion, false, null, null, false, null)
                repository.crearTarea(casaId, request)
                cargarTareas()
            } catch (e: Exception) {
                Log.e("TareasViewModel", "Error en crearTarea", e)
                _error.value = "Error al crear tarea: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun toggleCompletado(tarea: Tarea) {
        viewModelScope.launch {
            val request = TareaRequest(tarea.nombre, tarea.descripcion, !tarea.completado, null, null, null, null)
            try {
                repository.actualizarTarea(tarea.id, request)
                cargarTareas()
            } catch (e: Exception) {
                Log.e("TareasViewModel", "Error en toggleCompletado", e)
                _error.value = e.message
            }
        }
    }

    fun borrarTarea(tarea: Tarea) {
        viewModelScope.launch {
            try {
                repository.borrarTarea(tarea.id)
                cargarTareas()
            } catch (e: Exception) {
                Log.e("TareasViewModel", "Error en borrarTarea", e)
                _error.value = e.message
            }
        }
    }

    fun editarTarea(
        tarea: Tarea,
        nuevoNombre: String,
        nuevaDesc: String?,
        asignadoAId: Long?,
    ) {
        viewModelScope.launch {
            val request = TareaRequest(nuevoNombre, nuevaDesc, null, null, null, null, asignadoAId)
            try {
                repository.actualizarTarea(tarea.id, request)
                cargarTareas()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
}
