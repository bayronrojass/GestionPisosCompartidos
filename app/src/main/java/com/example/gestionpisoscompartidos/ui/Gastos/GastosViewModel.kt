package com.example.gestionpisoscompartidos.ui.gastos

import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.gestionpisoscompartidos.data.SessionManager
import com.example.gestionpisoscompartidos.data.repository.repositories.RepositoryCasa
import com.example.gestionpisoscompartidos.model.Gasto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PieChartData(
    val categoria: String, // Nombre de la persona
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

    // Paleta de colores del diseño (Orden: 1º, 2º, 3º, 4º...)
    private val colorPalette =
        listOf(
            Color(0xFFB1395B), // Rosa (Top 1)
            Color(0xFF8061A2), // Morado (Top 2)
            Color(0xFF93BBEC), // Azul (Top 3)
            Color(0xFF61995F), // Verde (Top 4)
            Color(0xFFFFD54F), // Amarillo (Extra)
            Color(0xFFFF8A65), // Naranja (Extra)
            Color(0xFF90A4AE), // Gris (Extra)
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

    private fun calcularEstadisticas(lista: List<Gasto>) {
        val total = lista.sumOf { it.importe }
        if (total == 0.0) {
            _stats.value = emptyList()
            return
        }

        // 1. Agrupar gastos por nombre de la persona
        val agrupado = lista.groupBy { it.pagadoPorNombre ?: "Desconocido" }

        // 2. Calcular totales y ordenar de MAYOR a MENOR gasto
        val datosOrdenados =
            agrupado
                .map { (nombre, gastos) ->
                    val totalPersona = gastos.sumOf { it.importe }
                    val porcentaje = (totalPersona / total).toFloat()
                    Pair(nombre, porcentaje)
                }.sortedByDescending { it.second } // Ordenar por porcentaje descendente

        // 3. Asignar colores según la posición en el ranking
        val estadisticasFinales =
            datosOrdenados.mapIndexed { index, (nombre, porcentaje) ->
                // Usamos el operador módulo (%) para ciclar colores si hay más personas que colores
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
