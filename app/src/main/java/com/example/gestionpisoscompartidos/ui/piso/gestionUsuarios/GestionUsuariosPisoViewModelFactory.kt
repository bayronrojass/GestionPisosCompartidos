package com.example.gestionpisoscompartidos.ui.piso.gestionUsuarios

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.gestionpisoscompartidos.data.SessionManager3

import com.example.gestionpisoscompartidos.data.repository.repositories.RepositoryCasa
import com.example.gestionpisoscompartidos.data.repository.repositories.RepositoryInvitacion

class GestionUsuariosPisoViewModelFactory(
    private val pisoRepository: RepositoryCasa,
    private val invitacionRepository: RepositoryInvitacion,
    private val sessionManager: SessionManager3,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GestionUsuariosPisoViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            // Pasa las dependencias al constructor del ViewModel
            return GestionUsuariosPisoViewModel(pisoRepository, invitacionRepository, sessionManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
