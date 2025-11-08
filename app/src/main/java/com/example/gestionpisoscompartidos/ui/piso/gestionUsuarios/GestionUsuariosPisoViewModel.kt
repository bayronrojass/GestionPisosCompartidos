package com.example.gestionpisoscompartidos.ui.piso.gestionUsuarios

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestionpisoscompartidos.data.SessionManager3
import com.example.gestionpisoscompartidos.data.repository.repositories.RepositoryCasa
import com.example.gestionpisoscompartidos.data.repository.repositories.RepositoryInvitacion
import com.example.gestionpisoscompartidos.model.InvitacionRequest
// ¡Importa el DTO de respuesta que tu backend envía!
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class GestionUsuariosPisoViewModel(
    private val pisoRepository: RepositoryCasa,
    private val invitacionRepository: RepositoryInvitacion,
    private val sessionManager: SessionManager3,
) : ViewModel() {
    private val _miembros = MutableStateFlow<List<MiembroPiso>>(emptyList())
    val miembros: StateFlow<List<MiembroPiso>> = _miembros

    // StateFlow para notificar a la UI (éxito/error)
    private val _accionResult = MutableStateFlow<String?>(null)
    val accionResult: StateFlow<String?> = _accionResult

    private var currentPisoId: Long = 0L // Para saber en qué piso estamos

    /**
     * El Fragment debe llamar a esta función para iniciar la carga de datos.
     */
    fun loadData(pisoId: Long) {
        if (pisoId == 0L || pisoId == currentPisoId) return // Evita recargas
        this.currentPisoId = pisoId
        loadMiembros()
    }

    private fun loadMiembros() {
        viewModelScope.launch {
            try {
                val currentUserId = sessionManager.fetchCurrentUserId()
                val token = sessionManager.fetchAuthToken()
                if (token == null) {
                    _accionResult.value = "Error: Sesión no iniciada"
                    return@launch
                }

                // --- ¡ESTO ES LO NUEVO! ---
                // 1. Llama al nuevo endpoint simple
                val response = pisoRepository.getPisoMiembros(token, currentPisoId)

                if (!response.isSuccessful) {
                    throw Exception("Error al cargar miembros: ${response.errorBody()?.string()}")
                }

                // 2. Extrae la lista de usuarios
                val usuariosDelPiso = response.body()!!
                // --- FIN DE LO NUEVO ---

                // 3. Tu lógica de mapeo. Como no tenemos admins, 'esAdmin' es siempre falso.
                val listaMiembrosUI =
                    usuariosDelPiso.map { usuario ->
                        MiembroPiso(
                            id = usuario.id,
                            nombre = usuario.nombre,
                            esAdmin = false, // <-- Simple, como pediste
                            esTu = usuario.id == currentUserId,
                            colorIndicator = getColorForUser(usuario.id),
                        )
                    }
                _miembros.value = listaMiembrosUI
            } catch (e: Exception) {
                _accionResult.value = "Error al cargar miembros: ${e.message}"
            }
        }
    }

    /**
     * Lógica para ENVIAR invitaciones por email (Esta ya estaba bien).
     */
    fun enviarInvitacion(email: String) {
        if (currentPisoId == 0L) {
            _accionResult.value = "Error: ID de piso no válido"
            return
        }

        viewModelScope.launch {
            try {
                val token = sessionManager.fetchAuthToken()
                if (token == null) {
                    _accionResult.value = "Error: Sesión no iniciada"
                    return@launch
                }

                val request = InvitacionRequest(currentPisoId, email)
                val response = invitacionRepository.crearInvitacion(token, request)

                if (response.isSuccessful) {
                    _accionResult.value = "Invitación enviada a $email"
                } else {
                    _accionResult.value = "Error: ${response.errorBody()?.string()}"
                }
            } catch (e: Exception) {
                _accionResult.value = "Error de red: ${e.message}"
            }
        }
    }

    /** * ¡MODIFICADO! Esta función ahora llama al backend.
     */
    fun removeMiembro(miembroId: Long) {
        viewModelScope.launch {
            try {
                val token = sessionManager.fetchAuthToken()
                if (token == null) {
                    _accionResult.value = "Error: Sesión no iniciada"
                    return@launch
                }

                // --- ¡MODO ONLINE ACTIVADO! ---
                val response = pisoRepository.removeMiembro(token, currentPisoId, miembroId)
                if (response.isSuccessful) {
                    // Si el backend tiene éxito, actualiza la UI localmente
                    _miembros.value = _miembros.value.filter { it.id != miembroId }
                    _accionResult.value = "Miembro eliminado"
                } else {
                    _accionResult.value = "Error al eliminar: ${response.errorBody()?.string()}"
                }
                // --- FIN DEL MODO ONLINE ---
            } catch (e: Exception) {
                _accionResult.value = "Error de red: ${e.message}"
            }
        }
    }

    /** Limpia el mensaje de resultado para que el Toast no se repita. */
    fun clearAccionResult() {
        _accionResult.value = null
    }

    private fun getColorForUser(id: Long): Int {
        val colors =
            listOf(
                android.R.color.holo_red_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_purple,
                android.R.color.holo_blue_light,
            )
        // Usa abs() para evitar el crash con IDs negativos (como -1)
        return colors[(kotlin.math.abs(id) % colors.size).toInt()]
    }
}
