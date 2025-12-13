package es.mirumi.es.ui.listas

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.mirumi.es.data.remote.NetworkModule
import es.mirumi.es.data.repository.repositories.RepositoryLista
import es.mirumi.es.data.repository.repositories.RepositoryCasa
import es.mirumi.es.model.Lista
import es.mirumi.es.model.Usuario
import es.mirumi.es.model.requests.ListaRequest
import kotlinx.coroutines.launch
import retrofit2.HttpException

class ListasViewModel(
    private val casaId: Long,
) : ViewModel() {
    private val repository = RepositoryLista(NetworkModule.listaApiService)
    private val repositoryCasa = RepositoryCasa(NetworkModule.casaApiService)

    private val _listas = MutableLiveData<List<Lista>>()
    val listas: LiveData<List<Lista>> = _listas

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _miembros = MutableLiveData<List<Usuario>>()
    val miembros: LiveData<List<Usuario>> = _miembros
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
        participantesIds: List<Long>,
        propietarioId: Long,
    ) {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                // Log para ver qué estamos enviando exactamente
                Log.d("DEBUG_LISTA", "Enviando -> Nombre: '$nombre', Desc: '$descripcion', Parts: $participantesIds")

                val request = ListaRequest(nombre, descripcion, participantesIds, propietarioId)
                val nuevaLista = repository.crearListaEnCasa(casaId, request)

                val listasActuales = _listas.value ?: emptyList()
                _listas.value = listasActuales + nuevaLista
            } catch (e: Exception) {
                // --- AQUÍ ESTÁ LA CLAVE PARA EL DEBUG ---
                if (e is HttpException) {
                    // Leemos el cuerpo del error que envía Spring Boot
                    val errorBody = e.response()?.errorBody()?.string()
                    Log.e("DEBUG_LISTA", "Error HTTP ${e.code()}: $errorBody")
                    _error.value = "Error del servidor: $errorBody"
                } else {
                    Log.e("DEBUG_LISTA", "Error genérico: ${e.message}", e)
                    _error.value = e.message ?: "Error al crear la lista"
                }
                // ----------------------------------------
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

    fun cargarMiembros(token: String) {
        viewModelScope.launch {
            try {
                // Asumiendo que repositoryCasa.getPisoMiembros devuelve Response<List<Usuario>>
                val response = repositoryCasa.getPisoMiembros(token, casaId)
                if (response.isSuccessful) {
                    _miembros.value = response.body() ?: emptyList()
                } else {
                    // Manejar error si es necesario
                }
            } catch (e: Exception) {
                // Manejar excepción
            }
        }
    }
}
