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
import com.example.gestionpisoscompartidos.model.eventRequest
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
                }
            } catch (e: Exception) {
                Log.e("CALENDARIO", "Excepción: ${e.message}")
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

    suspend fun createEvent(
        title: String,
        description: String,
        startDate: LocalDateTime,
        endDate: LocalDateTime,
    ): Boolean {
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
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            _createEventResult.value = false
            return false
        }
    }

    data class Event(
        val title: String,
        val description: String,
        val startDate: LocalDateTime,
        val endDate: LocalDateTime,
    )
}
