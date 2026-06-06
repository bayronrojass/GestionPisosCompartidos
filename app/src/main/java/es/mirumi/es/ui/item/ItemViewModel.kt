package es.mirumi.es.ui.item

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.mirumi.es.data.remote.NetworkModule
import es.mirumi.es.data.repository.repositories.RepositoryCatalogo
import es.mirumi.es.data.repository.repositories.RepositoryItem
import es.mirumi.es.model.CatalogoProducto
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

    private val catalogoRepo = RepositoryCatalogo(NetworkModule.catalogoApiService)

    private val _sugerencias = MutableLiveData<List<CatalogoProducto>>(emptyList())
    val sugerencias: LiveData<List<CatalogoProducto>> = _sugerencias

    private val _populares = MutableLiveData<List<CatalogoProducto>>(emptyList())
    val populares: LiveData<List<CatalogoProducto>> = _populares

    init {
        cargarItems()
        cargarPopulares()
    }

    private fun cargarPopulares() {
        viewModelScope.launch {
            _populares.value = catalogoRepo.getPopulares()
        }
    }

    fun buscarEnCatalogo(query: String) {
        viewModelScope.launch {
            if (query.length >= 2) {
                _sugerencias.value = catalogoRepo.buscarProductos(query)
            } else {
                _sugerencias.value = emptyList()
            }
        }
    }

    fun limpiarSugerencias() {
        _sugerencias.value = emptyList()
    }

    fun cargarItems() {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                _items.value = repository.getElementosByListaId(listaId)
            } catch (e: Exception) {
                _error.value = e.message ?: "Error cargando items"
                _items.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun crearElemento(
        nombre: String,
        descripcion: String?,
        iconoKey: String? = null,
    ) {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                val request = ElementoRequest(nombre, descripcion, false, 1, iconoKey)
                repository.crearElementoEnLista(listaId, request)
                cargarItems()
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
