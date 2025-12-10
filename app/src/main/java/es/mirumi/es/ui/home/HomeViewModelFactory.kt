package es.mirumi.es.ui.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import es.mirumi.es.data.SessionManager
import es.mirumi.es.data.remote.NetworkModule
import es.mirumi.es.data.repository.repositories.RepositoryCasa

class HomeViewModelFactory(
    private val context: Context,
    private val sessionManager: SessionManager,
    private val casaId: Long,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            val repositoryCasa = RepositoryCasa(NetworkModule.casaApiService)

            return HomeViewModel(
                repository = repositoryCasa,
                sessionManager = sessionManager,
                casaId = casaId,
                contentResolver = context.contentResolver,
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
