package com.example.gestionpisoscompartidos.ui.listas

import androidx.lifecycle.*
import com.example.gestionpisoscompartidos.data.remote.NetworkModule
import com.example.gestionpisoscompartidos.data.repository.repositories.RepositoryLista
import com.example.gestionpisoscompartidos.model.Lista
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

    private val _mostrarMensajeVacio = MutableLiveData<Boolean>()
    val mostrarMensajeVacio: LiveData<Boolean> = _mostrarMensajeVacio

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
                _mostrarMensajeVacio.value = resultado.isEmpty()
            } catch (e: Exception) {
                _error.value = e.message ?: "Error cargando listas"
                _listas.value = emptyList()
                _mostrarMensajeVacio.value = true //
            } finally {
                _isLoading.value = false
            }
        }
    }
}
