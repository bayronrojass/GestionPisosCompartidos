package com.example.gestionpisoscompartidos.ui.item

import androidx.lifecycle.*
import com.example.gestionpisoscompartidos.data.remote.NetworkModule
import com.example.gestionpisoscompartidos.data.repository.repositories.RepositoryItem
import com.example.gestionpisoscompartidos.model.Elemento
import kotlinx.coroutines.launch

class ItemViewModel(
    private val listaId: Long,
) : ViewModel() {
    private val repository = RepositoryItem(NetworkModule.itemApiService)

    private val _items = MutableLiveData<List<Elemento>>()
    val items: LiveData<List<Elemento>> = _items

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init {
        cargarItems()
    }

    fun cargarItems() {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                _items.value = repository.getElementosByListaId(listaId)
            } catch (e: Exception) {
                _error.value = e.message ?: "Error cargando items"
                _items.value = emptyList() // Asegura lista vacía en caso de error
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Aquí añadir funciones para:
    // fun addItem(nombre: String, descripcion: String?) { ... }
    // fun toggleItemCompletado(item: Elemento) { ... }
    // fun deleteItem(item: Elemento) { ... }
}
