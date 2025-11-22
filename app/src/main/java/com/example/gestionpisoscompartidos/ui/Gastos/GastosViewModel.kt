package com.example.gestionpisoscompartidos.ui.gastos

import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.gestionpisoscompartidos.data.SessionManager
import com.example.gestionpisoscompartidos.data.repository.repositories.RepositoryCasa
import com.example.gestionpisoscompartidos.model.Gasto
import com.example.gestionpisoscompartidos.model.GastoRequest // Asegúrate de tener este modelo creado
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PieChartData(
    val categoria: String,
    val porcentaje: Float,
    val textoPorcentaje: String,
    val color: Color,
)

class GastosViewModel(
    private val repository: RepositoryCasa,
    private val sessionManager: SessionManager,
    private val casaId: Long,
) : ViewModel() {
    private val _gastos = MutableStateFlow<List<Gasto>>(emptyList())
    val gastos: StateFlow<List<Gasto>> = _gastos.asStateFlow()

    private val _stats = MutableStateFlow<List<PieChartData>>(emptyList())
    val stats: StateFlow<List<PieChartData>> = _stats.asStateFlow()

    private val _mostrarEstadisticas = MutableStateFlow(false)
    val mostrarEstadisticas: StateFlow<Boolean> = _mostrarEstadisticas.asStateFlow()

    private val colorPalette =
        listOf(
            Color(0xFFB1395B),
            Color(0xFF8061A2),
            Color(0xFF93BBEC),
            Color(0xFF61995F),
            Color(0xFFFFD54F),
            Color(0xFFFF8A65),
            Color(0xFF90A4AE),
        )

    init {
        cargarGastos()
    }

    fun toggleVista(verEstadisticas: Boolean) {
        _mostrarEstadisticas.value = verEstadisticas
    }

    fun cargarGastos() {
        viewModelScope.launch {
            val token = sessionManager.fetchAuthToken() ?: return@launch
            try {
                val response = repository.getGastosCasa(token, casaId)
                if (response.isSuccessful) {
                    val lista = response.body() ?: emptyList()
                    _gastos.value = lista
                    calcularEstadisticas(lista)
                } else {
                    Log.e("GASTOS", "Error al cargar: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("GASTOS", "Error conexión: ${e.message}")
            }
        }
    }

    // --- ESTA ES LA FUNCIÓN QUE FALTABA ---
    fun crearGasto(
        nombre: String,
        importe: String,
        categoria: String,
    ) {
        viewModelScope.launch {
            val token = sessionManager.fetchAuthToken() ?: return@launch
            val userId = sessionManager.fetchCurrentUserId()
            val importeDouble = importe.toDoubleOrNull() ?: 0.0

            val request =
                GastoRequest(
                    nombre = nombre,
                    importe = importeDouble,
                    categoria = categoria,
                    pagadoPorId = userId,
                )

            try {
                val response = repository.crearGasto(token, casaId, request)
                if (response.isSuccessful) {
                    cargarGastos() // Recargar lista tras crear
                } else {
                    Log.e("GASTOS", "Error creando gasto: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("GASTOS", "Excepción creando gasto: ${e.message}")
            }
        }
    }

    private fun calcularEstadisticas(lista: List<Gasto>) {
        val total = lista.sumOf { it.importe }
        if (total == 0.0) {
            _stats.value = emptyList()
            return
        }

        val agrupado = lista.groupBy { it.pagadoPorNombre ?: "Desconocido" }

        val datosOrdenados =
            agrupado
                .map { (nombre, gastos) ->
                    val totalPersona = gastos.sumOf { it.importe }
                    val porcentaje = (totalPersona / total).toFloat()
                    Pair(nombre, porcentaje)
                }.sortedByDescending { it.second }

        val estadisticasFinales =
            datosOrdenados.mapIndexed { index, (nombre, porcentaje) ->
                val colorAsignado = colorPalette[index % colorPalette.size]
                PieChartData(
                    categoria = nombre,
                    porcentaje = porcentaje,
                    textoPorcentaje = "${(porcentaje * 100).toInt()}%",
                    color = colorAsignado,
                )
            }

        _stats.value = estadisticasFinales
    }
}

class GastosViewModelFactory(
    private val repository: RepositoryCasa,
    private val sessionManager: SessionManager,
    private val casaId: Long,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = GastosViewModel(repository, sessionManager, casaId) as T
}
