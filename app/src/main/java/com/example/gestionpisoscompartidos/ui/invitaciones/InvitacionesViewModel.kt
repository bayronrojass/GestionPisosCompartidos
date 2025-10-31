package com.example.gestionpisoscompartidos.ui.invitaciones

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestionpisoscompartidos.model.InvitacionRequest
import com.example.gestionpisoscompartidos.model.InvitacionResponse
import com.example.gestionpisoscompartidos.data.repository.repositories.RepositoryInvitacion
import kotlinx.coroutines.launch

class InvitacionesViewModel(
    private val repository: RepositoryInvitacion,
    private val authToken: String,
) : ViewModel() {
    private val _invitaciones = MutableLiveData<List<InvitacionResponse>>()
    val invitaciones: LiveData<List<InvitacionResponse>> = _invitaciones

    private val _accionExitosa = MutableLiveData<Boolean>()
    val accionExitosa: LiveData<Boolean> = _accionExitosa

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun fetchMisInvitaciones() {
        viewModelScope.launch {
            try {
                val response = repository.getMisInvitaciones(authToken)
                if (response.isSuccessful) {
                    _invitaciones.value = response.body()
                } else {
                    _error.value = "Error al cargar invitaciones"
                }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun enviarInvitacion(
        casaId: Long,
        emailDestinatario: String,
    ) {
        viewModelScope.launch {
            try {
                val request = InvitacionRequest(casaId, emailDestinatario)
                val response = repository.crearInvitacion(authToken, request)
                if (response.isSuccessful) {
                    _accionExitosa.value = true // Notifica a la UI
                } else {
                    _error.value = "Error al enviar invitación"
                }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun aceptarInvitacion(invitacionId: Long) {
        viewModelScope.launch {
            try {
                val response = repository.aceptarInvitacion(authToken, invitacionId)
                if (response.isSuccessful) {
                    // Refresca la lista de invitaciones
                    fetchMisInvitaciones()
                } else {
                    _error.value = "Error al aceptar"
                }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun rechazarInvitacion(invitacionId: Long) {
        viewModelScope.launch {
            try {
                val response = repository.rechazarInvitacion(authToken, invitacionId)
                if (response.isSuccessful) {
                    // Refresca la lista de invitaciones
                    fetchMisInvitaciones()
                } else {
                    _error.value = "Error al rechazar"
                }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
}
