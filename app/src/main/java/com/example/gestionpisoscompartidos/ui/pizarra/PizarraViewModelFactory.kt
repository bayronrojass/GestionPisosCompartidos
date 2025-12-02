package com.example.gestionpisoscompartidos.ui.pizarra

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class PizarraViewModelFactory(
    private val lienzoId: Long,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PizarraViewModel::class.java)) {
            return PizarraViewModel(lienzoId = lienzoId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
