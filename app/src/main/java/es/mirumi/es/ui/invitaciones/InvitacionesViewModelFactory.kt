package es.mirumi.es.ui.invitaciones

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import es.mirumi.es.data.SessionManager
import es.mirumi.es.data.repository.repositories.RepositoryInvitacion

/**
 * Factory para crear una instancia de InvitacionesViewModel
 * pasándole el repositorio y el session manager.
 */
class InvitacionesViewModelFactory(
    private val repository: RepositoryInvitacion,
    private val sessionManager: SessionManager,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InvitacionesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return InvitacionesViewModel(repository, sessionManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
