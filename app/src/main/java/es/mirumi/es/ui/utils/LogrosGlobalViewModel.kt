package es.mirumi.es.ui.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import es.mirumi.es.data.SessionManager
import es.mirumi.es.data.remote.NetworkModule
import es.mirumi.es.data.repository.repositories.RepositoryUsuario
import es.mirumi.es.model.dtos.UsuarioLogroDTO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LogrosGlobalViewModel(
    private val sessionManager: SessionManager,
) : ViewModel() {
    private val repository = RepositoryUsuario(NetworkModule.usuarioApiService)

    private val _nuevoLogro = MutableStateFlow<UsuarioLogroDTO?>(null)
    val nuevoLogro: StateFlow<UsuarioLogroDTO?> = _nuevoLogro.asStateFlow()

    private var medallasCompletadasAnteriormente: List<Long> = emptyList()
    private var esPrimeraCarga = true

    init {
        comprobarNuevosLogros(silencioso = true)
    }

    fun comprobarNuevosLogros(silencioso: Boolean = false) {
        viewModelScope.launch {
            val token = sessionManager.fetchAuthToken() ?: return@launch
            val userId = sessionManager.fetchCurrentUserId()

            try {
                val response = repository.getPerfilGamificacion(token, userId)
                if (response != null && response.isSuccessful) {
                    val perfil = response.body() ?: return@launch

                    val completadasAhora = perfil.logros.filter { it.estaCompletado }
                    val idsCompletadasAhora = completadasAhora.map { it.logroId }

                    if (esPrimeraCarga) {
                        medallasCompletadasAnteriormente = idsCompletadasAhora
                        esPrimeraCarga = false
                        return@launch
                    }

                    if (!silencioso && idsCompletadasAhora.size > medallasCompletadasAnteriormente.size) {
                        val idNueva = idsCompletadasAhora.firstOrNull { it !in medallasCompletadasAnteriormente }
                        val medallaNueva = completadasAhora.find { it.logroId == idNueva }

                        if (medallaNueva != null) {
                            _nuevoLogro.value = medallaNueva
                        }
                    }

                    medallasCompletadasAnteriormente = idsCompletadasAhora
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun cerrarPopup() {
        _nuevoLogro.value = null
    }
}

class LogrosGlobalViewModelFactory(
    private val sessionManager: SessionManager,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = LogrosGlobalViewModel(sessionManager) as T
}
