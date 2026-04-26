package es.mirumi.es.ui.pizarra.postits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import es.mirumi.es.data.SessionManager

class DraggableViewModelFactory(
    private val location: String,
    private val casaId: Long,
    private val sessionManager: SessionManager,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DraggableViewModel::class.java)) {
            return DraggableViewModel(location = location, casaId = casaId, sessionManager = sessionManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
