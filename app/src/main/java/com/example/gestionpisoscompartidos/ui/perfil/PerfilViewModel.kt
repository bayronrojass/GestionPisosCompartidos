package com.example.gestionpisoscompartidos.ui.perfil

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestionpisoscompartidos.data.SessionManager
import com.example.gestionpisoscompartidos.data.remote.NetworkModule
import com.example.gestionpisoscompartidos.data.repository.repositories.RepositoryUsuario
import com.example.gestionpisoscompartidos.model.Usuario
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

    private val _navigationEvent = MutableLiveData<String?>() // Para navegar al Login tras borrar
    val navigationEvent: LiveData<String?> = _navigationEvent

    init {
        cargarPerfil()
    }

    // UT: Ver Perfil
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

    // UT: Editar Perfil
    fun actualizarPerfil(
        nuevoNombre: String,
        nuevoCorreo: String,
    ) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val actualizado = repository.updateUsuario(userId, nuevoNombre, nuevoCorreo)
                _usuario.value = actualizado
                // Actualizamos también la sesión local por si acaso
                val token = sessionManager.fetchAuthToken()?.replace("Bearer ", "") ?: ""
                sessionManager.saveAuthData(token, userId, nuevoCorreo)
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    // UT: Eliminar Cuenta
    fun eliminarCuenta() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                repository.deleteUsuario(userId)
                cerrarSesion()
            } catch (e: Exception) {
                _error.value = e.message
                _isLoading.value = false
            }
        }
    }

    fun cerrarSesion() {
        sessionManager.logoutUser()
        _navigationEvent.value = "Login"
    }
}
