package es.mirumi.es.ui.perfil

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.mirumi.es.data.SessionManager
import es.mirumi.es.data.remote.NetworkModule
import es.mirumi.es.data.repository.repositories.RepositoryUsuario
import es.mirumi.es.model.Usuario
import kotlinx.coroutines.launch

class PerfilViewModel(
    private val sessionManager: SessionManager,
) : ViewModel() {
    private val repository = RepositoryUsuario(NetworkModule.usuarioApiService)
    private val userId = sessionManager.fetchCurrentUserId()

    private val _usuario = MutableLiveData<Usuario?>()
    val usuario: LiveData<Usuario?> = _usuario

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _toastMessage = MutableLiveData<String?>()
    val toastMessage: LiveData<String?> = _toastMessage
    private val _logoutEvent = MutableLiveData<String?>()
    val logoutEvent: LiveData<String?> = _logoutEvent

    private val _navigationEvent = MutableLiveData<String?>()
    val navigationEvent: LiveData<String?> = _navigationEvent

    init {
        cargarPerfil()
    }

    fun cargarPerfil() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val user = repository.getUsuario(userId)
                _usuario.value = user
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun actualizarPerfil(
        nuevoNombre: String,
        nuevoCorreo: String,
    ) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                repository.updateUsuario(userId, nuevoNombre, nuevoCorreo)
                cargarPerfil()
                // Actualizamos también la sesión local
                val token = sessionManager.fetchAuthToken()?.replace("Bearer ", "") ?: ""
                sessionManager.saveAuthData(token, userId, nuevoCorreo)
                _toastMessage.value = "Perfil actualizado correctamente"
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun eliminarCuenta() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                repository.deleteUsuario(userId)
                // Al borrar, llamamos a cerrar sesión con mensaje específico
                cerrarSesion("Cuenta eliminada correctamente")
            } catch (e: Exception) {
                _error.value = e.message
                _isLoading.value = false
            }
        }
    }

    fun cerrarSesion(mensaje: String = "Sesión cerrada con éxito") {
        sessionManager.logoutUser()
        // Emitimos el evento con el mensaje para que la UI navegue
        _logoutEvent.value = mensaje
    }
}
