package es.mirumi.es.ui.gastos

import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import es.mirumi.es.data.SessionManager
import es.mirumi.es.data.remote.NetworkModule
import es.mirumi.es.data.repository.repositories.RepositoryCasa
import es.mirumi.es.model.Gasto
import es.mirumi.es.model.dtos.BorradorGastoDTO
import es.mirumi.es.model.requests.AportacionRequest
import es.mirumi.es.model.requests.GastoRequest
import es.mirumi.es.model.requests.PagoBizumRequest
import es.mirumi.es.utils.Deuda
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import uriToFile
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

    // --- ESTADOS DE PANTALLA (StateFlows) ---
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

    // Estados IA Escáner
    private val _isScanningTicket = MutableStateFlow(false)
    val isScanningTicket: StateFlow<Boolean> = _isScanningTicket.asStateFlow()

    private val _borradorEscaneado = MutableStateFlow<BorradorGastoDTO?>(null)
    val borradorEscaneado: StateFlow<BorradorGastoDTO?> = _borradorEscaneado.asStateFlow()

    // Estado Bizum
    private val _mensajePago = MutableStateFlow<String?>(null)
    val mensajePago: StateFlow<String?> = _mensajePago.asStateFlow()

    // Colores Gráficos y Avatares
    private val colorPalette =
        listOf(
            Color(0xFF536DFE),
            Color(0xFFFF4081),
            Color(0xFF00E676),
            Color(0xFFFFD740),
            Color(0xFF7C4DFF),
            Color(0xFFFF6E40),
            Color(0xFF18FFFF),
            Color(0xFFFFAB40),
            Color(0xFFE040FB),
            Color(0xFF69F0AE),
        )

    init {
        cargarUsuariosCasa()
        cargarGastos()
    }

    // =========================================================================
    //  1. CARGA INICIAL Y NAVEGACIÓN
    // =========================================================================

    private fun cargarUsuariosCasa() {
        viewModelScope.launch {
            val token = sessionManager.fetchAuthToken() ?: return@launch
            try {
                val response = repository.getPisoMiembros(token, casaId)
                if (response.isSuccessful) {
                    val miembros = response.body()?.map { it.nombre } ?: emptyList()
                    _usuariosDetectados.value = miembros
                }
            } catch (e: Exception) {
                Log.e("GASTOS", "Error cargando miembros: ${e.message}")
            }
        }
    }

    fun cargarGastos() {
        viewModelScope.launch(Dispatchers.IO) {
            val token = sessionManager.fetchAuthToken() ?: return@launch
            try {
                val response = repository.getGastosCasa(token, casaId)
                if (response.isSuccessful) {
                    val lista = response.body() ?: emptyList()
                    listaCompleta = lista

                    aplicarFiltro(_filtroCategoria.value)
                    calcularEstadisticas(lista)

                    // Calculamos los saldos y luego con esos saldos generamos el plan de pagos
                    calcularSaldosYPlanPagos(lista)
                } else {
                    Log.e("GASTOS", "Error al cargar: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("GASTOS", "Error conexión: ${e.message}")
            }
        }
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

    // =========================================================================
    //  2. CREACIÓN Y EDICIÓN DE GASTOS
    // =========================================================================

    // Modificado para soportar múltiples aportaciones
    fun crearGasto(
        nombre: String,
        importe: String,
        categoria: String,
        aportaciones: List<AportacionRequest>,
        beneficiarios: List<String>,
    ) {
        viewModelScope.launch {
            val token = sessionManager.fetchAuthToken() ?: return@launch
            val importeDouble = importe.toDoubleOrNull() ?: 0.0

            val request =
                GastoRequest(
                    nombre = nombre,
                    importe = importeDouble,
                    categoria = categoria,
                    aportaciones = aportaciones,
                    beneficiarios = beneficiarios,
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

    // Sobrecarga por si tu UI todavía no manda las aportaciones (Evita que te rompa la app de momento)
    fun crearGasto(
        nombre: String,
        importe: String,
        categoria: String,
        beneficiarios: List<String>,
    ) {
        val userId = sessionManager.fetchCurrentUserId()
        val importeDouble = importe.toDoubleOrNull() ?: 0.0
        val aportacionUnica = listOf(AportacionRequest(userId, importeDouble))
        crearGasto(nombre, importe, categoria, aportacionUnica, beneficiarios)
    }

    // Modificado para soportar múltiples aportaciones
    fun editarGasto(
        gastoId: Long,
        nombre: String,
        importe: String,
        categoria: String,
        aportaciones: List<AportacionRequest>,
        beneficiarios: List<String>,
    ) {
        viewModelScope.launch {
            val token = sessionManager.fetchAuthToken() ?: return@launch
            val importeDouble = importe.toDoubleOrNull() ?: 0.0

            val request =
                GastoRequest(
                    nombre = nombre,
                    importe = importeDouble,
                    categoria = categoria,
                    aportaciones = aportaciones,
                    beneficiarios = beneficiarios,
                )

            try {
                val response = repository.editarGasto(token, casaId, gastoId, request)
                if (response.isSuccessful) {
                    cargarGastos()
                } else {
                    Log.e("GASTOS", "Error editando gasto: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("GASTOS", "Excepción editando gasto: ${e.message}")
            }
        }
    }

    // Sobrecarga de seguridad para la UI antigua
    fun editarGasto(
        gastoId: Long,
        nombre: String,
        importe: String,
        categoria: String,
        beneficiarios: List<String>,
    ) {
        val userId = sessionManager.fetchCurrentUserId()
        val importeDouble = importe.toDoubleOrNull() ?: 0.0
        val aportacionUnica = listOf(AportacionRequest(userId, importeDouble))
        editarGasto(gastoId, nombre, importe, categoria, aportacionUnica, beneficiarios)
    }

    // =========================================================================
    //  3. LÓGICA MATEMÁTICA Y CALCULADORA (Múltiples Pagadores)
    // =========================================================================

    // La he vuelto a hacer PÚBLICA por si tu GastosScreen la llama
    fun calcularPlanPagos() {
        // En lugar de usar la lógica antigua y rota, llamamos a la nueva unificada que es perfecta
        calcularSaldosYPlanPagos(listaCompleta)
    }

    // Función unificada que calcula Saldos y el Plan Mínimo de Pagos de forma eficiente
    private fun calcularSaldosYPlanPagos(lista: List<Gasto>) {
        val todosLosUsuarios = _usuariosDetectados.value
        if (todosLosUsuarios.isEmpty()) return

        // 1. Iniciamos los balances de todos a 0€
        val balances = todosLosUsuarios.associateWith { 0.0 }.toMutableMap()

        // 2. Repartimos gastos
        lista.forEach { gasto ->
            val beneficiariosDelGasto = gasto.beneficiarios?.takeIf { it.isNotEmpty() } ?: todosLosUsuarios
            if (beneficiariosDelGasto.isEmpty()) return@forEach

            val costoPorPersona = gasto.importe / beneficiariosDelGasto.size

            // Los que consumen restan de su balance
            beneficiariosDelGasto.forEach { b -> balances[b] = (balances[b] ?: 0.0) - costoPorPersona }

            // Los que pagan suman a su balance
            if (!gasto.aportaciones.isNullOrEmpty()) {
                gasto.aportaciones.forEach { aportacion ->
                    balances[aportacion.nombre] = (balances[aportacion.nombre] ?: 0.0) + aportacion.cantidad
                }
            } else if (gasto.pagadoPorNombre != null) {
                // Fallback para gastos antiguos que solo tenían un pagador
                balances[gasto.pagadoPorNombre] = (balances[gasto.pagadoPorNombre] ?: 0.0) + gasto.importe
            }
        }

        // 3. Actualizar la variable visual de Saldos
        val saldosCalculados =
            balances
                .map { (nombre, balance) ->
                    SaldoUsuario(nombre, balance, getColorPorNombreDinamico(nombre))
                }.sortedByDescending { it.cantidad }

        _saldos.value = saldosCalculados

        // 4. Generar el Plan de Pagos basándose en los saldos (Súper eficiente)
        val deudores = saldosCalculados.filter { it.cantidad < -0.01 }.map { it.copy() }.toMutableList()
        val acreedores = saldosCalculados.filter { it.cantidad > 0.01 }.map { it.copy() }.toMutableList()

        val nuevoPlan = mutableListOf<Deuda>()
        var d = 0
        var a = 0

        while (d < deudores.size && a < acreedores.size) {
            val deudor = deudores[d]
            val acreedor = acreedores[a]

            val deudaAPagar = minOf(abs(deudor.cantidad), acreedor.cantidad)
            if (deudaAPagar > 0.01) {
                nuevoPlan.add(Deuda(de = deudor.nombre, para = acreedor.nombre, cantidad = deudaAPagar))
            }

            deudores[d] = deudor.copy(cantidad = deudor.cantidad + deudaAPagar)
            acreedores[a] = acreedor.copy(cantidad = acreedor.cantidad - deudaAPagar)

            if (abs(deudores[d].cantidad) < 0.01) d++
            if (acreedores[a].cantidad < 0.01) a++
        }

        _planDePagos.value = nuevoPlan
    }

    // La he vuelto a hacer PÚBLICA por seguridad
    fun calcularEstadisticas(lista: List<Gasto> = listaCompleta) {
        val total = lista.sumOf { it.importe }
        if (total == 0.0) {
            _stats.value = emptyList()
            return
        }

        val agrupado = lista.groupBy { it.pagadoPorNombre ?: "Varios" }
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

    // La he vuelto a hacer PÚBLICA
    fun calcularSaldos(lista: List<Gasto> = listaCompleta) {
        calcularSaldosYPlanPagos(lista) // Llamamos al motor unificado para evitar repetir código
    }

    fun obtenerParticipantesGasto(gasto: Gasto): List<ParticipantePago> {
        val beneficiariosDelGasto = gasto.beneficiarios?.takeIf { it.isNotEmpty() } ?: _usuariosDetectados.value
        if (beneficiariosDelGasto.isEmpty()) return emptyList()

        val cuota = gasto.importe / beneficiariosDelGasto.size

        return beneficiariosDelGasto.map { nombre ->
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

    // =========================================================================
    //  4. IA ESCÁNER (TICKETS)
    // =========================================================================

    fun escanearTicketIA(
        context: android.content.Context,
        uri: android.net.Uri,
    ) {
        viewModelScope.launch {
            _isScanningTicket.value = true
            try {
                val token = sessionManager.fetchAuthToken() ?: throw Exception("Sin token")
                val tokenFormateado = if (token.startsWith("Bearer ")) token else "Bearer $token"

                val file = uriToFile(context, uri) ?: throw Exception("Error de imagen")
                val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

                val response = NetworkModule.casaApiService.escanearTicket(tokenFormateado, body)

                if (response.isSuccessful && response.body() != null) {
                    _borradorEscaneado.value = response.body()
                } else {
                    Log.e("GASTOS", "Error backend: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("GASTOS", "Excepción: ${e.message}")
            } finally {
                _isScanningTicket.value = false
            }
        }
    }

    fun limpiarBorrador() {
        _borradorEscaneado.value = null
    }

    // =========================================================================
    //  5. PAGOS Y BIZUM
    // =========================================================================

    fun realizarPago(
        acreedorId: Long,
        cantidad: Double,
        gastoId: Long? = null,
    ) {
        viewModelScope.launch {
            try {
                val token = sessionManager.fetchAuthToken() ?: ""
                val deudorId = sessionManager.fetchCurrentUserId()

                val request =
                    PagoBizumRequest(
                        deudorId = deudorId,
                        acreedorId = acreedorId,
                        cantidad = abs(cantidad),
                        gastoId = gastoId,
                    )

                val response = repository.notificarPagoBizum(token, casaId, request)

                if (response.isSuccessful) {
                    _mensajePago.value = "Pago notificado. Esperando confirmación del receptor."
                    cargarGastos()
                } else {
                    _mensajePago.value = "Error al notificar el pago: ${response.code()}"
                }
            } catch (e: Exception) {
                _mensajePago.value = "Error de red al procesar el pago"
                Log.e("GASTOS", "Excepción pago: ${e.message}")
            }
        }
    }

    fun limpiarMensajePago() {
        _mensajePago.value = null
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
