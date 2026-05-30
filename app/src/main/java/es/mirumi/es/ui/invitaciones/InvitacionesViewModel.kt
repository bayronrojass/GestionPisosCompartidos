package es.mirumi.es.ui.invitaciones

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.mirumi.es.data.SessionManager
import es.mirumi.es.data.repository.repositories.RepositoryInvitacion
import es.mirumi.es.model.requests.AccionInvitacionRequest
import es.mirumi.es.model.responses.InvitacionResponse
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class InvitacionesUiState(
    val isLoading: Boolean = false,
    val invitaciones: List<InvitacionResponse> = emptyList(),
    val error: String? = null,
)

sealed class InvitacionEvent {
    data class ShowToast(
        val message: String,
    ) : InvitacionEvent()

    data class NavigateToCasa(
        val casaId: Long,
        val casaNombre: String,
    ) : InvitacionEvent()
}

class InvitacionesViewModel(
    private val repository: RepositoryInvitacion,
    private val sessionManager: SessionManager,
) : ViewModel() {
    companion object {
        private const val TAG = "InvitacionesVM"
    }

    private val _uiState = MutableStateFlow(InvitacionesUiState())
    val uiState: StateFlow<InvitacionesUiState> = _uiState.asStateFlow()

    private val _events = Channel<InvitacionEvent>()
    val events = _events.receiveAsFlow()

    fun fetchMisInvitaciones() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val token = sessionManager.fetchAuthToken() ?: throw Exception("Usuario no autenticado")
                val usuarioId = sessionManager.fetchCurrentUserId()
                if (usuarioId == -1L) throw Exception("ID de usuario no encontrado")

                val response = repository.getMisInvitaciones(token, usuarioId)

                if (response.isSuccessful) {
                    _uiState.update {
                        it.copy(isLoading = false, invitaciones = response.body() ?: emptyList())
                    }
                } else {
                    val errorMsg = "Error: ${response.code()}"
                    Log.e(TAG, errorMsg)
                    _uiState.update { it.copy(isLoading = false, error = "No se pudieron cargar las invitaciones") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun aceptarInvitacion(invitacion: InvitacionResponse) {
        procesarInvitacion(invitacion, esAceptar = true)
    }

    fun rechazarInvitacion(invitacion: InvitacionResponse) {
        procesarInvitacion(invitacion, esAceptar = false)
    }

    private fun procesarInvitacion(
        invitacion: InvitacionResponse,
        esAceptar: Boolean,
    ) {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                val token = sessionManager.fetchAuthToken() ?: throw Exception("Usuario no autenticado")
                val usuarioId = sessionManager.fetchCurrentUserId()

                val request = AccionInvitacionRequest(usuarioId)
                val response =
                    if (esAceptar) {
                        repository.aceptarInvitacion(token, invitacion.id, request)
                    } else {
                        repository.rechazarInvitacion(token, invitacion.id, request)
                    }

                if (response.isSuccessful) {
                    if (esAceptar) {
                        sessionManager.saveCasaActiva(invitacion.casaId, invitacion.casaNombre)
                        _events.send(InvitacionEvent.NavigateToCasa(invitacion.casaId, invitacion.casaNombre))
                    } else {
                        _events.send(InvitacionEvent.ShowToast("Invitación rechazada"))
                        fetchMisInvitaciones()
                    }
                } else {
                    _events.send(InvitacionEvent.ShowToast("Error al procesar la invitación"))
                    _uiState.update { it.copy(isLoading = false) }
                }
            } catch (e: Exception) {
                _events.send(InvitacionEvent.ShowToast(e.message ?: "Error desconocido"))
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun errorShown() {
        _uiState.update { it.copy(error = null) }
    }
}
