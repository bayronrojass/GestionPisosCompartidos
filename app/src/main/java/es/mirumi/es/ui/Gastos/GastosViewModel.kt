package es.mirumi.es.ui.gastos

import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import es.mirumi.es.data.SessionManager
import es.mirumi.es.data.repository.repositories.RepositoryCasa
import es.mirumi.es.model.Gasto
import es.mirumi.es.model.requests.GastoRequest
import es.mirumi.es.utils.DebtCalculator
import es.mirumi.es.utils.Deuda
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.abs


data class PieChartData(
    val categoria: String,
    val porcentaje: Float,
    val textoPorcentaje: String,
    val color: Color,
)

data class SaldoUsuario(
    val nombre: String,
    val cantidad: Double,
    val colorAvatar: Color,
)

data class ParticipantePago(
    val nombre: String,
    val cantidad: Double,
    val colorAvatar: Color,
)

class GastosViewModel(
    private val repository: RepositoryCasa,
    private val sessionManager: SessionManager,
    private val casaId: Long,
) : ViewModel() {
    private var listaCompleta: List<Gasto> = emptyList()

    private val _gastos = MutableStateFlow<List<Gasto>>(emptyList())
    val gastos: StateFlow<List<Gasto>> = _gastos.asStateFlow()

    private val _stats = MutableStateFlow<List<PieChartData>>(emptyList())
    val stats: StateFlow<List<PieChartData>> = _stats.asStateFlow()

    private val _saldos = MutableStateFlow<List<SaldoUsuario>>(emptyList())
    val saldos: StateFlow<List<SaldoUsuario>> = _saldos.asStateFlow()

    private val _planDePagos = MutableStateFlow<List<Deuda>>(emptyList())
    val planDePagos: StateFlow<List<Deuda>> = _planDePagos.asStateFlow()

    private val _usuariosDetectados = MutableStateFlow<List<String>>(emptyList())
    val usuariosDetectados: StateFlow<List<String>> = _usuariosDetectados.asStateFlow()

    private val _mostrarEstadisticas = MutableStateFlow(false)
    val mostrarEstadisticas: StateFlow<Boolean> = _mostrarEstadisticas.asStateFlow()

    private val _filtroCategoria = MutableStateFlow("TODOS")
    val filtroCategoria: StateFlow<String> = _filtroCategoria.asStateFlow()


    private val colorPalette =
        listOf(
            Color(0xFF536DFE), // Azul eléctrico
            Color(0xFFFF4081), // Rosa fuerte
            Color(0xFF00E676), // Verde neón
            Color(0xFFFFD740), // Amarillo sol
            Color(0xFF7C4DFF), // Violeta profundo
            Color(0xFFFF6E40), // Naranja coral
            Color(0xFF18FFFF), // Cian brillante
            Color(0xFFFFAB40), // Naranja suave
            Color(0xFFE040FB), // Magenta
            Color(0xFF69F0AE), // Menta
        )

    init {
        cargarGastos()
    }

    fun toggleVista(verEstadisticas: Boolean) {
        _mostrarEstadisticas.value = verEstadisticas
    }

    fun aplicarFiltro(categoria: String) {
        _filtroCategoria.value = categoria
        if (categoria == "TODOS") {
            _gastos.value = listaCompleta
        } else {
            _gastos.value = listaCompleta.filter { it.categoria == categoria }
        }
    }

    fun cargarGastos() {
        viewModelScope.launch {
            val token = sessionManager.fetchAuthToken() ?: return@launch
            try {
                val response = repository.getGastosCasa(token, casaId)
                if (response.isSuccessful) {
                    val lista = response.body() ?: emptyList()
                    listaCompleta = lista

                    val usuarios = lista.mapNotNull { it.pagadoPorNombre }.distinct()
                    _usuariosDetectados.value = usuarios

                    aplicarFiltro(_filtroCategoria.value)
                    calcularEstadisticas(lista)
                    calcularSaldos(lista)
                } else {
                    Log.e("GASTOS", "Error al cargar: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("GASTOS", "Error conexión: ${e.message}")
            }
        }
    }

    fun crearGasto(
        nombre: String,
        importe: String,
        categoria: String,
        beneficiarios: List<String>,
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
                    cargarGastos()
                } else {
                    Log.e("GASTOS", "Error creando gasto: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("GASTOS", "Excepción creando gasto: ${e.message}")
            }
        }
    }

    /**
     * Calcula quién debe a quién basándose en los gastos.
     * Soporta que un gasto sea para personas específicas (si el modelo Gasto lo tuviera).
     */
    fun calcularPlanPagos() {
        viewModelScope.launch(Dispatchers.Default) {
            val gastosActuales = listaCompleta
            val todosLosUsuarios = _usuariosDetectados.value

            if (todosLosUsuarios.isEmpty() || gastosActuales.isEmpty()) {
                _planDePagos.value = emptyList()
                return@launch
            }

            val deudasBrutas = mutableListOf<Deuda>()

            gastosActuales.forEach { gasto ->
                val pagador = gasto.pagadoPorNombre ?: return@forEach

                val beneficiarios = todosLosUsuarios // gasto.beneficiarios ?: todosLosUsuarios

                val numBeneficiarios = beneficiarios.size
                if (numBeneficiarios == 0) return@forEach

                val importePorPersona = gasto.importe / numBeneficiarios

                beneficiarios.forEach { usuario ->
                    if (usuario != pagador) {
                        deudasBrutas.add(
                            Deuda(
                                de = usuario,
                                para = pagador,
                                cantidad = importePorPersona,
                            ),
                        )
                    }
                }
            }

            val planOptimizado = DebtCalculator.simplificar(deudasBrutas)
            _planDePagos.value = planOptimizado
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
            datosOrdenados.map { (nombre, porcentaje) ->
                PieChartData(
                    categoria = nombre,
                    porcentaje = porcentaje,
                    textoPorcentaje = "${(porcentaje * 100).toInt()}%",
                    color = getColorPorNombreDinamico(nombre),
                )
            }

        _stats.value = estadisticasFinales
    }

    private fun calcularSaldos(lista: List<Gasto>) {
        if (lista.isEmpty()) {
            _saldos.value = emptyList()
            _usuariosDetectados.value = emptyList()
            return
        }

        val totalGastado = lista.sumOf { it.importe }
        val usuarios = lista.mapNotNull { it.pagadoPorNombre }.distinct()
        _usuariosDetectados.value = usuarios

        val numUsuarios = if (usuarios.isEmpty()) 1 else usuarios.size

        val mediaPorPersona = totalGastado / numUsuarios

        val saldosCalculados =
            usuarios
                .map { usuario ->
                    val pagadoPorUsuario =
                        lista.filter { it.pagadoPorNombre == usuario }.sumOf { it.importe }
                    val balance = pagadoPorUsuario - mediaPorPersona

                    SaldoUsuario(
                        nombre = usuario,
                        cantidad = balance,
                        colorAvatar = getColorPorNombreDinamico(usuario),
                    )
                }.sortedByDescending { it.cantidad }

        _saldos.value = saldosCalculados
    }

    fun obtenerParticipantesGasto(importeTotal: Double): List<ParticipantePago> {
        val usuarios = _usuariosDetectados.value
        if (usuarios.isEmpty()) return emptyList()

        val cuota = importeTotal / usuarios.size

        return usuarios.map { nombre ->
            ParticipantePago(
                nombre = nombre,
                cantidad = cuota,
                colorAvatar = getColorPorNombreDinamico(nombre),
            )
        }
    }

    fun getColorPorNombreDinamico(nombre: String): Color {
        if (nombre.isEmpty() || nombre == "Desconocido") return Color.Gray
        val hash = abs(nombre.hashCode())
        val index = hash % colorPalette.size
        return colorPalette[index]
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
