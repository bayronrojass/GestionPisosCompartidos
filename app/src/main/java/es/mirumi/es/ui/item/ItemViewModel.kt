package es.mirumi.es.ui.item

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.mirumi.es.data.remote.NetworkModule
import es.mirumi.es.data.repository.repositories.RepositoryItem
import es.mirumi.es.model.Elemento
import es.mirumi.es.model.requests.ElementoRequest
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

    fun crearElemento(
        nombre: String,
        descripcion: String?,
    ) {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                val request = ElementoRequest(nombre, descripcion, false, 1)
                repository.crearElementoEnLista(listaId, request)
                cargarItems()
                // Añade el nuevo elemento a la lista local
                // val itemsActuales = _items.value ?: emptyList()
                // _items.value = itemsActuales + nuevoElemento
            } catch (e: Exception) {
                _error.value = e.message ?: "Error al crear el item"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleItemCompletado(elemento: Elemento) {
        _error.value = null
        viewModelScope.launch {
            val estadoNuevo = !elemento.completado
            val request =
                ElementoRequest(
                    nombre = elemento.nombre,
                    descripcion = null,
                    completado = estadoNuevo,
                )
            try {
                if (elemento.id != null) {
                    repository.actualizarElemento(elemento.id, request)
                }
                cargarItems()
            } catch (e: Exception) {
                _error.value = e.message ?: "Error al actualizar item"
            }
        }
    }

    private fun actualizarListaLocal(itemActualizado: Elemento) {
        val itemsActuales = _items.value?.toMutableList() ?: mutableListOf()
        val index = itemsActuales.indexOfFirst { it.id == itemActualizado.id }
        if (index != -1) {
            itemsActuales[index] = itemActualizado
            _items.value = itemsActuales
        }
    }

    fun borrarElemento(elemento: Elemento) {
        _error.value = null
        viewModelScope.launch {
            try {
                if (elemento.id != null) {
                    repository.borrarElemento(elemento.id)
                }
                cargarItems()
            } catch (e: Exception) {
                _error.value = e.message ?: "Error al borrar item"
            }
        }
    }

    fun actualizarNombreDescripcion(
        elemento: Elemento,
        nuevoNombre: String,
        nuevaDescripcion: String?,
    ) {
        _error.value = null
        viewModelScope.launch {
            val request =
                ElementoRequest(
                    nombre = nuevoNombre,
                    descripcion = nuevaDescripcion,
                    completado = elemento.completado,
                )

            try {
                if (elemento.id != null) {
                    repository.actualizarElemento(elemento.id, request)
                }
                cargarItems()
            } catch (e: Exception) {
                _error.value = e.message ?: "Error al actualizar item"
            }
        }
    }

    fun actualizarCantidad(
        elemento: Elemento,
        nuevaCantidad: Int,
    ) {
        if (nuevaCantidad < 1) return

        viewModelScope.launch {
            val request =
                ElementoRequest(
                    nombre = elemento.nombre,
                    descripcion = elemento.descripcion,
                    completado = elemento.completado,
                    cantidad = nuevaCantidad,
                )

            try {
                if (elemento.id != null) {
                    repository.actualizarElemento(elemento.id, request)
                }
                cargarItems()
            } catch (e: Exception) {
                _error.value = "Error al actualizar cantidad"
            }
        }
    }
}
