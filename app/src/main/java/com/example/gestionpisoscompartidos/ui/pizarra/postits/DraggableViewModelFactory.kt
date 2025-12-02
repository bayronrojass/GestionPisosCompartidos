package com.example.gestionpisoscompartidos.ui.pizarra.postits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class DraggableViewModelFactory(
    private val location: String,
    private val casaId: Long,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DraggableViewModel::class.java)) {
            return DraggableViewModel(location = location, casaId = casaId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
