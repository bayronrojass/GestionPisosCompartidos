package es.mirumi.es.ui.ranking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import es.mirumi.es.data.SessionManager
import es.mirumi.es.data.remote.NetworkModule
import es.mirumi.es.data.repository.repositories.RepositoryCasa
import es.mirumi.es.model.dtos.UsuarioRankingDTO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RankingViewModel(
    private val sessionManager: SessionManager,
    private val casaId: Long,
) : ViewModel() {
    private val repository = RepositoryCasa(NetworkModule.casaApiService)

    private val _ranking = MutableStateFlow<List<UsuarioRankingDTO>>(emptyList())
    val ranking: StateFlow<List<UsuarioRankingDTO>> = _ranking.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        cargarRanking()
    }

    private fun cargarRanking() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val token = sessionManager.fetchAuthToken() ?: return@launch
                val response = repository.getRankingCasa(token, casaId)
                if (response != null && response.isSuccessful) {
                    _ranking.value = response.body() ?: emptyList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
}

class RankingViewModelFactory(
    private val sessionManager: SessionManager,
    private val casaId: Long,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = RankingViewModel(sessionManager, casaId) as T
}
