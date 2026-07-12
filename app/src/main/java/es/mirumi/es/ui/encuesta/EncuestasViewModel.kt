package es.mirumi.es.ui.encuestas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.mirumi.es.data.repository.repositories.RepositoryEncuesta
import es.mirumi.es.model.requests.EncuestaRequest
import es.mirumi.es.model.responses.EncuestaResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EncuestasViewModel(
    private val casaId: Long,
) : ViewModel() {
    private val repository = RepositoryEncuesta()

    private val _encuestas = MutableStateFlow<List<EncuestaResponse>>(emptyList())
    val encuestas: StateFlow<List<EncuestaResponse>> = _encuestas.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        cargarEncuestas()
    }

    fun cargarEncuestas() {
        viewModelScope.launch {
            _isLoading.value = true

            // Opcional: Vaciar la lista antigua para que no haya efecto fantasma
            _encuestas.value = emptyList()

            try {
                // Aquí el repository usará el token FRESCO del usuario actual
                val response = repository.obtenerEncuestas(casaId)
                if (response.isSuccessful) {
                    _encuestas.update { response.body() ?: emptyList() }
                } else {
                    println("Error obteniendo encuestas: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                println("Excepción cargando encuestas: ${e.message}")
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun votar(
        encuestaId: Long,
        opcionId: Long,
    ) {
        viewModelScope.launch {
            try {
                val response = repository.votar(encuestaId, opcionId)
                if (response.isSuccessful) {
                    cargarEncuestas()
                } else {
                    println("Error al votar: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                println("Excepción al votar: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    fun crearEncuesta(
        titulo: String,
        opciones: List<String>,
        colorHex: String,
    ) {
        viewModelScope.launch {
            try {
                val request = EncuestaRequest(titulo, opciones, colorHex)
                val response = repository.crearEncuesta(casaId, request)
                if (response.isSuccessful) {
                    cargarEncuestas()
                } else {
                    println("Error al crear encuesta: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                println("Excepción creando encuesta: ${e.message}")
                e.printStackTrace()
            }
        }
    }
}
