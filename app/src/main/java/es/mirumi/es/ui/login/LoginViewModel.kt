package es.mirumi.es.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.mirumi.es.data.repository.repositories.RepositoryLogin
import es.mirumi.es.model.requests.LoginRequest
import es.mirumi.es.model.responses.LoginResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class LoginUiState {
    object Idle : LoginUiState()

    object Loading : LoginUiState()

    data class Success(
        val response: LoginResponse,
    ) : LoginUiState()

    data class Error(
        val message: String,
    ) : LoginUiState()
}

class LoginViewModel(
    private val repository: RepositoryLogin,
) : ViewModel() {
    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun login(
        email: String,
        password: String,
    ) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = LoginUiState.Error("Por favor, introduce tu correo y contraseña.")
            return
        }

        _uiState.value = LoginUiState.Loading

        viewModelScope.launch {
            try {
                val response = repository.login(LoginRequest(email, password))
                _uiState.value = LoginUiState.Success(response)
            } catch (e: Exception) {
                _uiState.value = LoginUiState.Error(e.message ?: "Ocurrió un error desconocido al iniciar sesión.")
            }
        }
    }

    fun resetState() {
        _uiState.value = LoginUiState.Idle
    }
}
