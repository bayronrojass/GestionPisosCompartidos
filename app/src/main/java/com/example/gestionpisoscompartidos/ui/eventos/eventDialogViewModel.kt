package com.example.gestionpisoscompartidos.ui.eventos

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.Date

class EventDialogViewModel : ViewModel() {
    private val _createEventResult = MutableStateFlow<Boolean?>(null)
    // private val repository = RepositoryEvento()

    fun buttonState(
        eventTitle: String,
        pickedDate: Date?,
    ): Boolean = eventTitle.isNotEmpty() && pickedDate != null
}
