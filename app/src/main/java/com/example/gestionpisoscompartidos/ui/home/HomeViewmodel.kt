package com.example.gestionpisoscompartidos.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestionpisoscompartidos.data.SessionManager
import com.example.gestionpisoscompartidos.data.repository.repositories.RepositoryCasa
import com.example.gestionpisoscompartidos.model.Evento
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

class HomeViewModel(
    private val repository: RepositoryCasa,
    private val sessionManager: SessionManager,
    private val casaId: Long,
) : ViewModel() {
    // Lista completa de eventos descargados
    private val _eventos = MutableStateFlow<List<Evento>>(emptyList())
    val eventos: StateFlow<List<Evento>> = _eventos

    // Día seleccionado en la barra horizontal
    private val _fechaSeleccionada = MutableStateFlow(LocalDate.now())
    val fechaSeleccionada: StateFlow<LocalDate> = _fechaSeleccionada

    // Eventos filtrados para el día seleccionado
    private val _eventosDelDia = MutableStateFlow<List<Evento>>(emptyList())
    val eventosDelDia: StateFlow<List<Evento>> = _eventosDelDia

    init {
        cargarEventos()
    }

    fun cargarEventos() {
        viewModelScope.launch {
            val token = sessionManager.fetchAuthToken() ?: return@launch
            try {
                // Asumiendo que tienes este método en tu repositorio
                val response = repository.getEventosCasa(token, casaId)
                if (response.isSuccessful) {
                    _eventos.value = response.body() ?: emptyList()
                    filtrarEventosPorFecha(_fechaSeleccionada.value)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun seleccionarFecha(fecha: LocalDate) {
        _fechaSeleccionada.value = fecha
        filtrarEventosPorFecha(fecha)
    }

    private fun filtrarEventosPorFecha(fecha: LocalDate) {
        val listaFiltrada =
            _eventos.value.filter { evento ->
                // Parseamos la fecha del evento (asumiendo ISO-8601)
                try {
                    val fechaEvento = LocalDate.parse(evento.fechaInicio.substring(0, 10))
                    fechaEvento.isEqual(fecha)
                } catch (e: Exception) {
                    false
                }
            }
        _eventosDelDia.value = listaFiltrada
    }
}
