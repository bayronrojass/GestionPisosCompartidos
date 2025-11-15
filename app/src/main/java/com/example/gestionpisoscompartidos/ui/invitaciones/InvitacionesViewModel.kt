package com.example.gestionpisoscompartidos.ui.invitaciones

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestionpisoscompartidos.model.AccionInvitacionRequest
import com.example.gestionpisoscompartidos.data.SessionManager
import com.example.gestionpisoscompartidos.data.repository.repositories.RepositoryInvitacion
import com.example.gestionpisoscompartidos.model.InvitacionResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class InvitacionesViewModel(
    private val repository: RepositoryInvitacion,
    private val sessionManager: SessionManager,
) : ViewModel() {
    // 2. Define un TAG para encontrar tus logs fácilmente
    companion object {
        private const val TAG = "InvitacionesVM"
    }

    private val _invitaciones = MutableStateFlow<List<InvitacionResponse>>(emptyList())
    val invitaciones: StateFlow<List<InvitacionResponse>> = _invitaciones

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun fetchMisInvitaciones() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val token = sessionManager.fetchAuthToken()
                if (token == null) throw Exception("Usuario no autenticado")

                val usuarioId = sessionManager.fetchCurrentUserId()
                if (usuarioId == -1L) throw Exception("ID de usuario no encontrado")

                val response = repository.getMisInvitaciones(token, usuarioId)

                if (response.isSuccessful) {
                    _invitaciones.value = response.body() ?: emptyList()
                } else {
                    val errorMsg = "Error al cargar invitaciones: ${response.code()} - ${response.message()} - ${response.errorBody()?.string()}"
                    Log.e(TAG, errorMsg)

                    throw Exception(errorMsg)
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun aceptarInvitacion(invitacionId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val token = sessionManager.fetchAuthToken()
                if (token == null) throw Exception("Usuario no autenticado")

                val usuarioId = sessionManager.fetchCurrentUserId()
                if (usuarioId == -1L) throw Exception("ID de usuario no encontrado")

                val request = AccionInvitacionRequest(usuarioId)

                val response = repository.aceptarInvitacion(token, invitacionId, request)

                if (response.isSuccessful) {
                    // Si tiene éxito, refresca la lista de invitaciones pendientes
                    fetchMisInvitaciones()
                } else {
                    // También puedes añadir un Log.e aquí
                    val errorMsg = "Error al aceptar: ${response.code()} - ${response.errorBody()?.string()}"
                    Log.e(TAG, errorMsg)
                    throw Exception(errorMsg)
                }
            } catch (e: Exception) {
                _error.value = e.message
                _isLoading.value = false
            }
        }
    }

    fun rechazarInvitacion(invitacionId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val token = sessionManager.fetchAuthToken()
                if (token == null) throw Exception("Usuario no autenticado")

                val usuarioId = sessionManager.fetchCurrentUserId()
                if (usuarioId == -1L) throw Exception("ID de usuario no encontrado")

                // 3. Crea el objeto request
                val request = AccionInvitacionRequest(usuarioId)

                val response = repository.rechazarInvitacion(token, invitacionId, request)

                if (response.isSuccessful) {
                    fetchMisInvitaciones()
                } else {
                    val errorMsg = "Error al rechazar: ${response.code()} - ${response.errorBody()?.string()}"
                    Log.e(TAG, errorMsg)
                    throw Exception(errorMsg)
                }
            } catch (e: Exception) {
                _error.value = e.message
                _isLoading.value = false
            }
        }
    }

    /** Limpia el mensaje de error para que el Toast no se repita. */
    fun clearError() {
        _error.value = null
    }
}
