package es.mirumi.es.ui.encuestas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class EncuestasViewModelFactory(
    private val casaId: Long,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EncuestasViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EncuestasViewModel(casaId = casaId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
