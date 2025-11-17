package com.example.gestionpisoscompartidos.ui.eventos

import androidx.lifecycle.ViewModel
import java.util.Date

class EventDialogViewModel : ViewModel() {
    fun buttonState(
        eventTitle: String,
        pickedDate: Date?,
    ): Boolean = eventTitle.isNotEmpty() && pickedDate != null
}
