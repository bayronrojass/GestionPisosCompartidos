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

// Modelo simple para el gráfico
data class PieChartData(
    val categoria: String,
    val porcentaje: Float, // 0.0 a 1.0
    val textoPorcentaje: String, // "64%"
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

    // Estado para controlar qué vista mostramos (Lista o Estadísticas)
    private val _mostrarEstadisticas = MutableStateFlow(false)
    val mostrarEstadisticas: StateFlow<Boolean> = _mostrarEstadisticas.asStateFlow()

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
        if (total == 0.0) return

        // Agrupar por persona que pagó (o categoría, según tu diseño visual parece por persona)
        val agrupado = lista.groupBy { it.pagadoPor }

        val datosGrafico =
            agrupado
                .map { (nombre, gastos) ->
                    val totalPersona = gastos.sumOf { it.importe }
                    val porcentaje = (totalPersona / total).toFloat()

                    PieChartData(
                        categoria = nombre.toString(),
                        porcentaje = porcentaje,
                        textoPorcentaje = "${(porcentaje * 100).toInt()}%",
                        color = getColorPorNombre(nombre.toString()),
                    )
                }.sortedByDescending { it.porcentaje }

        _stats.value = datosGrafico
    }

    private fun getColorPorNombre(nombre: String): Color {
        // Colores fijos basados en tu diseño
        return when {
            nombre.contains("Natalia", true) -> Color(0xFFB1395B) // Rojo
            nombre.contains("Daniel", true) -> Color(0xFF8061A2) // Morado
            nombre.contains("Marta", true) -> Color(0xFF93BBEC) // Azul
            nombre.contains("Raquel", true) -> Color(0xFF61995F) // Verde
            else -> Color.Gray
        }
    }
}

// Factory necesaria para pasar parámetros al ViewModel
class GastosViewModelFactory(
    private val repository: RepositoryCasa,
    private val sessionManager: SessionManager,
    private val casaId: Long,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = GastosViewModel(repository, sessionManager, casaId) as T
}
