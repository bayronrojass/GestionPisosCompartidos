package es.mirumi.es.ui.perfil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.mirumi.es.data.SessionManager
import es.mirumi.es.data.remote.NetworkModule
import es.mirumi.es.data.repository.repositories.RepositoryUsuario
import es.mirumi.es.model.Usuario
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// 1. Estado de la UI unificado
data class PerfilUiState(
    val isLoading: Boolean = false,
    val usuario: Usuario? = null,
    val error: String? = null,
)

// 2. Eventos de un solo uso (Toasts, Navegación)
sealed class PerfilEvent {
    data class ShowToast(
        val message: String,
    ) : PerfilEvent()

    data class LogoutSuccess(
        val message: String,
    ) : PerfilEvent()
}

class PerfilViewModel(
    private val sessionManager: SessionManager,
) : ViewModel() {
    private val repository = RepositoryUsuario(NetworkModule.usuarioApiService)
    private val userId = sessionManager.fetchCurrentUserId()

    // Manejo de Estado (StateFlow)
    private val _uiState = MutableStateFlow(PerfilUiState())
    val uiState: StateFlow<PerfilUiState> = _uiState.asStateFlow()

    // Manejo de Eventos Únicos (Channel)
    private val _events = Channel<PerfilEvent>()
    val events = _events.receiveAsFlow()

    init {
        cargarPerfil()
    }

    fun cargarPerfil() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val user = repository.getUsuario(userId)
                _uiState.update { it.copy(isLoading = false, usuario = user) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun actualizarPerfil(
        nuevoNombre: String,
        nuevoCorreo: String,
    ) {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                repository.updateUsuario(userId, nuevoNombre, nuevoCorreo)

                val user = repository.getUsuario(userId)
                _uiState.update { it.copy(isLoading = false, usuario = user) }

                // Actualizamos también la sesión local
                val token = sessionManager.fetchAuthToken()?.replace("Bearer ", "") ?: ""
                sessionManager.saveAuthData(token, userId, nuevoCorreo)

                // Disparamos el Toast
                _events.send(PerfilEvent.ShowToast("Perfil actualizado correctamente"))
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun eliminarCuenta() {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                repository.deleteUsuario(userId)
                cerrarSesion("Cuenta eliminada correctamente")
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun cerrarSesion(mensaje: String = "Sesión cerrada con éxito") {
        sessionManager.logoutUser()
        viewModelScope.launch {
            _events.send(PerfilEvent.LogoutSuccess(mensaje))
        }
    }

    // Limpia el error de la UI una vez mostrado
    fun errorShown() {
        _uiState.update { it.copy(error = null) }
    }
}
