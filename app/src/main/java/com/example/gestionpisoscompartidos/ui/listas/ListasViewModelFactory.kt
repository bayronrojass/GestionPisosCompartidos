package com.example.gestionpisoscompartidos.ui.listas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ListasViewModelFactory(
    private val casaId: Long,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ListasViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ListasViewModel(casaId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
