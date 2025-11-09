package com.example.gestionpisoscompartidos.ui.invitaciones

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestionpisoscompartidos.data.SessionManager3
import com.example.gestionpisoscompartidos.data.repository.repositories.RepositoryInvitacion
import com.example.gestionpisoscompartidos.model.InvitacionResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class InvitacionesViewModel(
    private val repository: RepositoryInvitacion,
    private val sessionManager: SessionManager3,
) : ViewModel() {
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

                // Llama al repositorio (que llama a GET /invitaciones/me)
                val response = repository.getMisInvitaciones(token)

                if (response.isSuccessful) {
                    _invitaciones.value = response.body() ?: emptyList()
                } else {
                    throw Exception("Error al cargar invitaciones: ${response.code()}")
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

                // Llama al repositorio (que llama a POST /{id}/aceptar)
                val response = repository.aceptarInvitacion(token, invitacionId)

                if (response.isSuccessful) {
                    // Si tiene éxito, refresca la lista de invitaciones pendientes
                    fetchMisInvitaciones()
                } else {
                    throw Exception("Error al aceptar: ${response.code()}")
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

                // Llama al repositorio (que llama a POST /{id}/rechazar)
                val response = repository.rechazarInvitacion(token, invitacionId)

                if (response.isSuccessful) {
                    // Si tiene éxito, refresca la lista
                    fetchMisInvitaciones()
                } else {
                    throw Exception("Error al rechazar: ${response.code()}")
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
