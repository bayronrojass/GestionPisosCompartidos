package com.example.gestionpisoscompartidos.ui.invitaciones

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.gestionpisoscompartidos.data.SessionManager
import com.example.gestionpisoscompartidos.data.repository.repositories.RepositoryInvitacion

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
