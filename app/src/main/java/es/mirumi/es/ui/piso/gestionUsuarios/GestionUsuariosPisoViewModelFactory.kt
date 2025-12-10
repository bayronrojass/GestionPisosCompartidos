package es.mirumi.es.ui.piso.gestionUsuarios

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import es.mirumi.es.data.SessionManager

import es.mirumi.es.data.repository.repositories.RepositoryCasa
import es.mirumi.es.data.repository.repositories.RepositoryInvitacion

class GestionUsuariosPisoViewModelFactory(
    private val pisoRepository: RepositoryCasa,
    private val invitacionRepository: RepositoryInvitacion,
    private val sessionManager: SessionManager,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GestionUsuariosPisoViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            // Pasa las dependencias al constructor del ViewModel
            return GestionUsuariosPisoViewModel(
                pisoRepository,
                invitacionRepository,
                sessionManager,
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
