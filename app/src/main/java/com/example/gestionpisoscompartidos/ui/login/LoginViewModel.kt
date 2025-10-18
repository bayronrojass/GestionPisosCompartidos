package com.example.gestionpisoscompartidos.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestionpisoscompartidos.model.LoginRequest
import com.example.gestionpisoscompartidos.model.LoginResponse
import com.example.gestionpisoscompartidos.data.repository.Repository
import kotlinx.coroutines.launch

// Necesitarás una factoría de ViewModel si no usas Hilt o Koin
// o añadir la anotación @HiltViewModel si usas Hilt.

/**
 * ViewModel para gestionar la lógica de la pantalla de inicio de sesión.
 */
class LoginViewModel(
    private val repository: Repository,
) : ViewModel() {
    private val _loginResult = MutableLiveData<LoginResponse?>()
    val loginResult: LiveData<LoginResponse?> = _loginResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    /**
     * Intenta iniciar sesión.
     */
    fun login(
        email: String,
        password: String,
    ) {
        // Validación básica de campos
        if (email.isBlank() || password.isBlank()) {
            _error.value = "Por favor, introduce tu correo y contraseña."
            return
        }

        _isLoading.value = true // Activar spinner de carga
        _error.value = null // Limpiar errores anteriores

        viewModelScope.launch {
            try {
                val request = LoginRequest(email, password)

                // Llamar al repositorio (que llama a la API)
                val response = repository.login(request)

                _loginResult.value = response // Notificar éxito al Fragmento
            } catch (e: Exception) {
                // Notificar error al Fragmento
                _error.value = e.message ?: "Ocurrió un error desconocido al iniciar sesión."
            } finally {
                _isLoading.value = false // Desactivar spinner de carga
            }
        }
    }

    // Método para limpiar el resultado del login una vez procesado por el fragmento
    fun clearLoginResult() {
        _loginResult.value = null
    }
}
