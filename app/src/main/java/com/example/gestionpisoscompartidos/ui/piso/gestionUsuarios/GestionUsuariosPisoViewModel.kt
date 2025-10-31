package com.example.gestionpisoscompartidos.ui.piso.gestionUsuarios

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestionpisoscompartidos.data.SessionManager3
import com.example.gestionpisoscompartidos.data.repository.repositories.RepositoryCasa
import com.example.gestionpisoscompartidos.data.repository.repositories.RepositoryInvitacion
import com.example.gestionpisoscompartidos.model.InvitacionRequest
import  com.example.gestionpisoscompartidos.model.Usuario
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
                // Obtén el ID real del usuario desde el SessionManager
                val currentUserId = sessionManager.fetchCurrentUserId()
                val token = sessionManager.fetchAuthToken()
                if (token == null) {
                    _accionResult.value = "Error: Sesión no iniciada"
                    return@launch
                }

                // --- TODO: REEMPLAZA ESTO CON DATOS REALES ---
                // Necesitarás un endpoint en tu CasaAPI/RepositoryCasa
                // para obtener los detalles y miembros de un piso.
                /*
                val pisoDetailsResponse = pisoRepository.getPisoDetails(token, currentPisoId)
                if (!pisoDetailsResponse.isSuccessful) throw Exception("Error al cargar piso")
                val usuariosDelPiso = pisoDetailsResponse.body()!!.miembros
                val adminIds = pisoDetailsResponse.body()!!.administradores.map { it.id }
                */

                // --- INICIO DE DATOS DE EJEMPLO (Usando el ID real) ---

                val adminIds = listOf(1L)
                val usuariosDelPiso: List<Usuario> =
                    listOf(
                        Usuario(1L, "Manolo (Ejemplo)", "manolo@mail.com"),
                        Usuario(currentUserId, "Tú Mismo (Ejemplo)", "me@mail.com"),
                        Usuario(3L, "Paula (Ejemplo)", "paula@mail.com"),
                    )
                // --- FIN DE DATOS DE EJEMPLO ---



                val listaMiembrosUI =
                    usuariosDelPiso.map { usuario ->
                        MiembroPiso(
                            id = usuario.id,
                            nombre = usuario.nombre,
                            esAdmin = usuario.id in adminIds,
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
     * Lógica para ENVIAR invitaciones por email.
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

    /** Lógica para eliminar un miembro (a conectar con API) */
    fun removeMiembro(miembroId: Long) {
        viewModelScope.launch {
            try {
                val token = sessionManager.fetchAuthToken()
                if (token == null) {
                    _accionResult.value = "Error: Sesión no iniciada"
                    return@launch
                }

                val response = pisoRepository.removeMiembro(token, currentPisoId, miembroId)
                if (response.isSuccessful) {
                    _miembros.value = _miembros.value.filter { it.id != miembroId }
                    _accionResult.value = "Miembro eliminado (Ejemplo)"
                } else {
                    _accionResult.value = "Error al eliminar"
                }
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
        return colors[(id % colors.size).toInt()]
    }
}
