package es.mirumi.es.ui.encuestas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import es.mirumi.es.data.SessionManager

class EncuestasViewModelFactory(
    private val casaId: Long,
    private val sessionManager: SessionManager, // <-- Lo pasamos por aquí
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EncuestasViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EncuestasViewModel(casaId, sessionManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
