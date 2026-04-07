package es.mirumi.es.ui.piso.listaPisos

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.mirumi.es.data.SessionManager
import es.mirumi.es.data.repository.repositories.RepositoryCasa
import es.mirumi.es.model.Casa
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ListaCasasUiState(
    val isLoading: Boolean = false,
    val casas: List<Casa> = emptyList(),
    val error: String? = null,
)

class ListaCasasViewModel(
    private val repositoryCasa: RepositoryCasa,
    private val sessionManager: SessionManager,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ListaCasasUiState())
    val uiState: StateFlow<ListaCasasUiState> = _uiState.asStateFlow()

    fun cargarCasas(casasIniciales: List<Casa>) {
        if (casasIniciales.isNotEmpty()) {
            _uiState.update { it.copy(casas = casasIniciales, isLoading = false) }
        } else {
            refreshCasas()
        }
    }

    fun refreshCasas() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val token = sessionManager.fetchAuthToken() ?: throw Exception("Sesión expirada")
                val usuarioId = sessionManager.fetchCurrentUserId()

                val response = repositoryCasa.getMisCasas(token, usuarioId)

                if (response.isSuccessful) {
                    _uiState.update { it.copy(isLoading = false, casas = response.body() ?: emptyList()) }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Error del servidor: ${response.code()}") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Error desconocido") }
                Log.e("ListaCasasVM", "Error al cargar casas", e)
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
