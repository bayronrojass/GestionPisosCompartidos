package com.example.gestionpisoscompartidos.ui.home

import android.content.ContentResolver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.gestionpisoscompartidos.data.SessionManager
import com.example.gestionpisoscompartidos.data.repository.repositories.RepositoryCasa

class HomeViewModelFactory(
    private val repository: RepositoryCasa,
    private val sessionManager: SessionManager,
    private val casaId: Long,
    private val contentResolver: ContentResolver, // Add this parameter
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(repository, sessionManager, casaId, contentResolver) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
