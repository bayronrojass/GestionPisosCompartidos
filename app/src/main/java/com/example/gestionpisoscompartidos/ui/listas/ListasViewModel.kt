package com.example.gestionpisoscompartidos.ui.listas

import androidx.lifecycle.*
import com.example.gestionpisoscompartidos.data.remote.NetworkModule
import com.example.gestionpisoscompartidos.data.repository.repositories.RepositoryLista
import com.example.gestionpisoscompartidos.model.Lista
import com.example.gestionpisoscompartidos.model.ListaRequest
import kotlinx.coroutines.launch

class ListasViewModel(
    private val casaId: Long,
) : ViewModel() {
    private val repository = RepositoryLista(NetworkModule.listaApiService)

    private val _listas = MutableLiveData<List<Lista>>()
    val listas: LiveData<List<Lista>> = _listas

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    // private val _mostrarMensajeVacio = MutableLiveData<Boolean>()
    // val mostrarMensajeVacio: LiveData<Boolean> = _mostrarMensajeVacio

    init {
        cargarListas()
    }

    fun cargarListas() {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                val resultado = repository.getListasByCasaId(casaId)
                _listas.value = resultado
                // _mostrarMensajeVacio.value = resultado.isEmpty()
            } catch (e: Exception) {
                _error.value = e.message ?: "Error cargando listas"
                _listas.value = emptyList()
                // _mostrarMensajeVacio.value = true
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun crearLista(
        nombre: String,
        descripcion: String?,
    ) {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                val request = ListaRequest(nombre, descripcion)
                val nuevaLista = repository.crearListaEnCasa(casaId, request)

                // Añade la nueva lista a la lista existente y notifica a la UI
                val listasActuales = _listas.value ?: emptyList()
                _listas.value = listasActuales + nuevaLista
                // Opcionalmente, recarga todo desde el servidor
                // cargarListas()
            } catch (e: Exception) {
                _error.value = e.message ?: "Error al crear la lista"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun borrarLista(lista: Lista) {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                repository.borrarLista(lista.id)

                // Elimina la lista de la UI localmente
                val listasActuales = _listas.value?.toMutableList() ?: mutableListOf()
                listasActuales.remove(lista)
                _listas.value = listasActuales
            } catch (e: Exception) {
                _error.value = e.message ?: "Error al borrar la lista"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
