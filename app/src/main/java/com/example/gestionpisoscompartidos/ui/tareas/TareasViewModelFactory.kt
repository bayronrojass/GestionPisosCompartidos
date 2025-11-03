package com.example.gestionpisoscompartidos.ui.tareas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class TareasViewModelFactory(
    private val casaId: Long,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TareasViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TareasViewModel(casaId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
