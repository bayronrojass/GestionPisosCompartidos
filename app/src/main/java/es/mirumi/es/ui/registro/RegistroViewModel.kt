package es.mirumi.es.ui.registro

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.mirumi.es.data.repository.repositories.RepositoryRegistro
import es.mirumi.es.model.requests.RegistroRequest
import es.mirumi.es.model.responses.LoginResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class RegistroUiState {
    object Idle : RegistroUiState()

    object Loading : RegistroUiState()

    data class Success(
        val response: LoginResponse,
    ) : RegistroUiState()

    data class Error(
        val message: String,
    ) : RegistroUiState()
}

class RegistroViewModel(
    private val repository: RepositoryRegistro,
) : ViewModel() {
    private val _uiState = MutableStateFlow<RegistroUiState>(RegistroUiState.Idle)
    val uiState: StateFlow<RegistroUiState> = _uiState.asStateFlow()

    fun register(
        nombre: String,
        correo: String,
        contrasena: String,
        contrasenaConfirm: String,
    ) {
        val trimmedNombre = nombre.trim()
        val trimmedCorreo = correo.trim()

        when {
            trimmedNombre.isBlank() ||
                trimmedCorreo.isBlank() ||
                contrasena.isBlank() ||
                contrasenaConfirm.isBlank() -> {
                _uiState.value = RegistroUiState.Error("Todos los campos son obligatorios.")
                return
            }
            !Patterns.EMAIL_ADDRESS.matcher(trimmedCorreo).matches() -> {
                _uiState.value = RegistroUiState.Error("Introduce un correo válido.")
                return
            }
            contrasena.length < 6 -> {
                _uiState.value = RegistroUiState.Error("La contraseña debe tener al menos 6 caracteres.")
                return
            }
            contrasena != contrasenaConfirm -> {
                _uiState.value = RegistroUiState.Error("Las contraseñas no coinciden.")
                return
            }
        }

        _uiState.value = RegistroUiState.Loading

        viewModelScope.launch {
            try {
                val response =
                    repository.register(
                        RegistroRequest(
                            nombre = trimmedNombre,
                            correo = trimmedCorreo,
                            contrasena = contrasena,
                        ),
                    )
                _uiState.value = RegistroUiState.Success(response)
            } catch (e: Exception) {
                _uiState.value = RegistroUiState.Error(e.message ?: "Ocurrió un error desconocido al registrar.")
            }
        }
    }

    fun resetState() {
        _uiState.value = RegistroUiState.Idle
    }
}
