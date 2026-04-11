package es.mirumi.es.ui.codigoQR

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.mirumi.es.data.SessionManager
import es.mirumi.es.data.remote.NetworkModule
import es.mirumi.es.data.repository.repositories.RepositoryCasa
import es.mirumi.es.model.requests.JoinCasaRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CodigoQRViewModel(
    private val sessionManager: SessionManager,
) : ViewModel() {
    // Nos conectamos a tu API de casas
    private val repositoryCasa = RepositoryCasa(NetworkModule.casaApiService)

    private val _mensajeQR = MutableStateFlow<String?>(null)
    val mensajeQR: StateFlow<String?> = _mensajeQR

    fun procesarQRUnirseCasa(casaIdLeido: Long) {
        viewModelScope.launch {
            try {
                // Recuperamos el token
                val token = sessionManager.fetchAuthToken() ?: return@launch

                // Recuperamos el ID del usuario que está usando la app
                val miUsuarioId = sessionManager.fetchCurrentUserId()

                // ¡AQUÍ ESTÁ LA CORRECCIÓN! Le pasamos el usuarioId, no el casaId
                val request = JoinCasaRequest(usuarioId = miUsuarioId)

                // Hacemos la llamada (el casaIdLeido va en el medio para la URL)
                val response = repositoryCasa.joinCasa(token, casaIdLeido, request)

                if (response.isSuccessful) {
                    _mensajeQR.value = "¡Te has unido a la casa con éxito!"
                } else {
                    _mensajeQR.value = "Error al unirse: Código ${response.code()}"
                }
            } catch (e: Exception) {
                _mensajeQR.value = "Error de conexión al servidor"
            }
        }
    }

    fun limpiarMensaje() {
        _mensajeQR.value = null
    }
}
