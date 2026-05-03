package es.mirumi.es.ui.encuestas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.mirumi.es.data.repository.repositories.RepositoryEncuesta
import es.mirumi.es.model.requests.EncuestaRequest
import es.mirumi.es.model.responses.EncuestaResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class EncuestasViewModel(
    private val repository: RepositoryEncuesta = RepositoryEncuesta(),
    private val casaId: Long,
) : ViewModel() {
    private val _encuestas = MutableStateFlow<List<EncuestaResponse>>(emptyList())
    val encuestas: StateFlow<List<EncuestaResponse>> = _encuestas

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        cargarEncuestas()
    }

    fun cargarEncuestas() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = repository.obtenerEncuestas(casaId)
                if (response.isSuccessful) {
                    _encuestas.value = response.body() ?: emptyList()
                }
            } catch (e: Exception) {
                // Manejar error
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
                }
            } catch (e: Exception) {
                // Manejar error
            }
        }
    }

    fun crearEncuesta(
        titulo: String,
        opciones: List<String>,
    ) {
        viewModelScope.launch {
            try {
                val request = EncuestaRequest(titulo, opciones)
                val response = repository.crearEncuesta(casaId, request)
                if (response.isSuccessful) {
                    cargarEncuestas()
                }
            } catch (e: Exception) {
                // Manejar error
            }
        }
    }
}
