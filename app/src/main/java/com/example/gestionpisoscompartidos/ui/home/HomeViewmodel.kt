package com.example.gestionpisoscompartidos.ui.home

import android.content.ContentResolver
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestionpisoscompartidos.data.SessionManager
import com.example.gestionpisoscompartidos.data.remote.NetworkModule
import com.example.gestionpisoscompartidos.data.repository.repositories.RepositoryCasa
import com.example.gestionpisoscompartidos.data.repository.repositories.RepositoryEvento
import com.example.gestionpisoscompartidos.model.Evento
import com.example.gestionpisoscompartidos.model.requests.eventRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.temporal.TemporalAdjusters

class HomeViewModel(
    private val repository: RepositoryCasa,
    private val sessionManager: SessionManager,
    private val casaId: Long,
    private val contentResolver: ContentResolver,
) : ViewModel() {
    private val _eventos = MutableStateFlow<List<Evento>>(emptyList())
    val eventos: StateFlow<List<Evento>> = _eventos

    private val _fechaSeleccionada = MutableStateFlow(LocalDate.now())
    val fechaSeleccionada: StateFlow<LocalDate> = _fechaSeleccionada

    private val _eventosDelDia = MutableStateFlow<List<Evento>>(emptyList())
    val eventosDelDia: StateFlow<List<Evento>> = _eventosDelDia

    private val _createEventResult = MutableStateFlow<Boolean?>(null)
    val createEventResult: StateFlow<Boolean?> = _createEventResult.asStateFlow()

    private val _deleteEventResult = MutableStateFlow<Boolean?>(null)
    val deleteEventResult: StateFlow<Boolean?> = _deleteEventResult.asStateFlow()

    private val _updateEventResult = MutableStateFlow<Boolean?>(null)
    val updateEventResult: StateFlow<Boolean?> = _updateEventResult.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        cargarEventos()
    }

    fun cargarEventos() {
        viewModelScope.launch {
            val token = sessionManager.fetchAuthToken() ?: return@launch
            try {
                Log.d("CALENDARIO", "Pidiendo eventos para casaId: $casaId")

                val response = repository.getEventosCasa(token, casaId)

                if (response.isSuccessful) {
                    val listaRecibida = response.body() ?: emptyList()
                    Log.d("CALENDARIO", "Eventos recibidos: ${listaRecibida.size}")
                    listaRecibida.forEach { Log.d("CALENDARIO", "Evento: ${it.nombre} - Fecha: ${it.fechaInicio}") }

                    _eventos.value = listaRecibida
                    seleccionarFecha(_fechaSeleccionada.value)
                } else {
                    Log.e("CALENDARIO", "Error al cargar eventos: ${response.code()}")
                    _error.value = "Error al cargar eventos: ${response.code()}"
                }
            } catch (e: Exception) {
                Log.e("CALENDARIO", "Excepción: ${e.message}")
                _error.value = "Error: ${e.message}"
                e.printStackTrace()
            }
        }
    }

    fun seleccionarFecha(fecha: LocalDate) {
        _fechaSeleccionada.value = fecha
        filtrarEventosPorFecha(fecha)
    }

    private fun filtrarEventosPorFecha(fecha: LocalDate) {
        val finDeSemana = fecha.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))

        val listaFiltrada =
            _eventos.value
                .filter { evento ->
                    val fechaEvento = parseFechaSegura(evento.fechaInicio)
                    !fechaEvento.isBefore(fecha) && !fechaEvento.isAfter(finDeSemana)
                }.sortedBy { it.fechaInicio }

        _eventosDelDia.value = listaFiltrada
    }

    private fun parseFechaSegura(fechaString: String): LocalDate =
        try {
            if (fechaString.length >= 10) {
                LocalDate.parse(fechaString.substring(0, 10))
            } else {
                LocalDate.now()
            }
        } catch (e: Exception) {
            LocalDate.now()
        }

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

    private suspend fun createEvent(
        title: String,
        description: String,
        startDate: LocalDateTime,
        endDate: LocalDateTime,
    ): Boolean =
        try {
            val eventRepository = RepositoryEvento(NetworkModule.eventoAPIService)
            val eventRequest =
                eventRequest(
                    nombre = title,
                    descripcion = description,
                    fechaInicio = startDate.toString(),
                    fechaFin = endDate.toString(),
                    creadoPor = sessionManager.fetchCurrentUserId(),
                )

            val response = eventRepository.crearEvento(eventRequest, casaId, contentResolver)
            kotlinx.coroutines.delay(1000L)
            cargarEventos()
            _createEventResult.value = true
            true
        } catch (e: Exception) {
            e.printStackTrace()
            _error.value = "Error creando evento: ${e.message}"
            _createEventResult.value = false
            false
        }

    private suspend fun eliminarEvento(eventoId: Long): Boolean =
        try {
            val eventRepository = RepositoryEvento(NetworkModule.eventoAPIService)
            eventRepository.eliminarEvento(eventoId)
            true
        } catch (e: Exception) {
            Log.e("HomeViewModel", "Error eliminando evento", e)
            _error.value = "Error eliminando evento: ${e.message}"
            false
        }

    fun eliminar(eventoId: Long) {
        viewModelScope.launch {
            try {
                val success = eliminarEvento(eventoId)
                if (success) {
                    cargarEventos()
                    _deleteEventResult.value = true
                } else {
                    _deleteEventResult.value = false
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "No se ha podido eliminar evento", e)
                _error.value = "Error eliminando evento: ${e.message}"
                _deleteEventResult.value = false
            }
        }
    }

    fun actualizarEvento(
        evento: Evento,
        nuevoNombre: String,
        nuevaDescripcion: String?,
        nuevaFechaInicio: LocalDateTime,
        nuevaFechaFin: LocalDateTime,
    ) {
        _error.value = null
        _updateEventResult.value = null

        viewModelScope.launch {
            try {
                val eventRepository = RepositoryEvento(NetworkModule.eventoAPIService)
                val request =
                    eventRequest(
                        nombre = nuevoNombre,
                        descripcion = nuevaDescripcion,
                        fechaInicio = nuevaFechaInicio.toString(),
                        fechaFin = nuevaFechaFin.toString(),
                        creadoPor = evento.creadoPor,
                    )

                if (evento.id != null) {
                    eventRepository.actualizarEvento(evento.id, request)
                    cargarEventos()
                    _updateEventResult.value = true
                } else {
                    _error.value = "El evento no tiene ID válido"
                    _updateEventResult.value = false
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Error al actualizar evento"
                _updateEventResult.value = false
            }
        }
    }

    fun clearCreateEventResult() {
        _createEventResult.value = null
    }

    fun clearDeleteEventResult() {
        _deleteEventResult.value = null
    }

    fun clearUpdateEventResult() {
        _updateEventResult.value = null
    }

    fun clearError() {
        _error.value = null
    }
}
