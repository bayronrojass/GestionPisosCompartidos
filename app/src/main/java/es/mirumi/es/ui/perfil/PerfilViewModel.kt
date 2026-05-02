package es.mirumi.es.ui.perfil

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.mirumi.es.data.SessionManager
import es.mirumi.es.data.remote.NetworkModule
import es.mirumi.es.data.repository.repositories.RepositoryUsuario
import es.mirumi.es.model.Usuario
import es.mirumi.es.model.dtos.PerfilGamificacionDTO
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import uriToFile

data class PerfilUiState(
    val isLoading: Boolean = false,
    val usuario: Usuario? = null,
    val gamificacion: PerfilGamificacionDTO? = null,
    val error: String? = null,
)

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

    private val _uiState = MutableStateFlow(PerfilUiState())
    val uiState: StateFlow<PerfilUiState> = _uiState.asStateFlow()

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
                val token = sessionManager.fetchAuthToken() ?: ""
                val responseGamificacion = repository.getPerfilGamificacion(token, userId)

                val gamificacionData =
                    if (responseGamificacion != null && responseGamificacion.isSuccessful) {
                        responseGamificacion.body()
                    } else {
                        Log.e("GAMIFICACION", "Error al obtener logros: ${responseGamificacion?.code()}")
                        null
                    }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        usuario = user,
                        gamificacion = gamificacionData,
                    )
                }
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
                cargarPerfil()

                val token = sessionManager.fetchAuthToken()?.replace("Bearer ", "") ?: ""
                sessionManager.saveAuthData(token, userId, nuevoCorreo)

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

    fun errorShown() {
        _uiState.update { it.copy(error = null) }
    }

    fun subirFotoPerfil(
        context: Context,
        uri: Uri,
    ) {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val file = uriToFile(context, uri) ?: throw Exception("No se pudo procesar la imagen")
                val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

                val response = NetworkModule.usuarioApiService.subirFotoPerfil(userId, body)

                if (response.isSuccessful) {
                    cargarPerfil()
                    _events.send(PerfilEvent.ShowToast("Foto de perfil actualizada"))
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Error al subir la foto al servidor") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun eliminarFotoPerfil() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val response = NetworkModule.usuarioApiService.eliminarFotoPerfil(userId)
                if (response.isSuccessful) {
                    cargarPerfil()
                    _events.send(PerfilEvent.ShowToast("Foto de perfil eliminada"))
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Error al eliminar la foto") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}
