package es.mirumi.es.ui.piso.listaPisos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import es.mirumi.es.data.SessionManager
import es.mirumi.es.data.repository.repositories.RepositoryCasa

class ListaCasasViewModelFactory(
    private val repositoryCasa: RepositoryCasa,
    private val sessionManager: SessionManager,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ListaCasasViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ListaCasasViewModel(repositoryCasa, sessionManager) as T
        }
        throw IllegalArgumentException("Clase ViewModel desconocida")
    }
}
