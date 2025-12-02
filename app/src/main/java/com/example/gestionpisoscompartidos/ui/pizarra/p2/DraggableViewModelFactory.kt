package com.example.gestionpisoscompartidos.ui.pizarra.p2

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class DraggableViewModelFactory(
    private val location: String,
    private val casaId: Long,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DraggableViewModel::class.java)) {
            // Pasamos la localización y el repositorio al ViewModel
            return DraggableViewModel(location = location, casaId = casaId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
