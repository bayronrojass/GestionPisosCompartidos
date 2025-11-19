package com.example.gestionpisoscompartidos.ui.eventos

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class eventosViewModel(
    application: Application,
) : AndroidViewModel(application) {
    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events: StateFlow<List<Event>> = _events

    private val _selectedDate = MutableStateFlow<String>("")
    val selectedDate: StateFlow<String> = _selectedDate

    fun addEvent(event: Event) {
        viewModelScope.launch {
            _events.value = _events.value + event
        }
    }

    fun selectDate(date: String) {
        _selectedDate.value = date
    }
}

data class Event(
    val id: String,
    val title: String,
    val description: String,
    val date: String,
    val time: String,
)
