package com.example.gestionpisoscompartidos.ui.eventos

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestionpisoscompartidos.data.SessionManager
import com.example.gestionpisoscompartidos.data.remote.NetworkModule
import com.example.gestionpisoscompartidos.data.repository.repositories.RepositoryEvento
import com.example.gestionpisoscompartidos.model.requests.eventRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class EventosViewModel(
    application: Application,
) : AndroidViewModel(application) {
    private val repository = RepositoryEvento(NetworkModule.eventoAPIService)
    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events: StateFlow<List<Event>> = _events

    private val contentResolver = application.contentResolver

    private val _selectedDate = MutableStateFlow<String>("")

    val selectedDate: StateFlow<String> = _selectedDate

    private val _createEventResult = MutableStateFlow<Boolean?>(null)

    private val sessionManager = SessionManager(application)

    fun crea(
        title: String,
        description: String,
        startDate: LocalDateTime,
        endDate: LocalDateTime,
    ) {
        viewModelScope.launch {
            createEvent(title, description, startDate, endDate)
        }
    }

    suspend fun createEvent(
        title: String,
        description: String,
        startDate: LocalDateTime,
        endDate: LocalDateTime,
    ) {
        try {
            val eventRequest =
                eventRequest(
                    nombre = title,
                    descripcion = description,
                    fechaInicio = startDate.toString(),
                    fechaFin = endDate.toString(),
                    creadoPor = sessionManager.fetchCurrentUserId(),
                )

            // val response = repository.crearEvento(eventRequest, contentResolver)
            _createEventResult.value = true
            true
        } catch (e: Exception) {
            e.printStackTrace()
            _createEventResult.value = false
            false
        }
    }
}

data class Event(
    val title: String,
    val description: String,
    val startDate: LocalDateTime,
    val endDate: LocalDateTime,
)
