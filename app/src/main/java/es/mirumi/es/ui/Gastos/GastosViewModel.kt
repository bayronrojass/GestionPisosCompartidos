package es.mirumi.es.ui.gastos

import android.content.Context
import android.net.Uri
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

data class UsuarioPiso(
    val id: Long,
    val nombre: String,
)

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

    private val _miNombre = MutableStateFlow<String>("")
    val miNombre: StateFlow<String> = _miNombre.asStateFlow()

    private val _gastos = MutableStateFlow<List<Gasto>>(emptyList())
    val gastos: StateFlow<List<Gasto>> = _gastos.asStateFlow()

    private val _stats = MutableStateFlow<List<PieChartData>>(emptyList())
    val stats: StateFlow<List<PieChartData>> = _stats.asStateFlow()

    private val _saldos = MutableStateFlow<List<SaldoUsuario>>(emptyList())
    val saldos: StateFlow<List<SaldoUsuario>> = _saldos.asStateFlow()

    private val _planDePagos = MutableStateFlow<List<Deuda>>(emptyList())
    val planDePagos: StateFlow<List<Deuda>> = _planDePagos.asStateFlow()

    // AHORA GUARDAMOS OBJETOS COMPLETOS, NO SOLO STRINGS
    private val _usuariosDetectados = MutableStateFlow<List<UsuarioPiso>>(emptyList())
    val usuariosDetectados: StateFlow<List<UsuarioPiso>> = _usuariosDetectados.asStateFlow()

    private val _mostrarEstadisticas = MutableStateFlow(false)
    val mostrarEstadisticas: StateFlow<Boolean> = _mostrarEstadisticas.asStateFlow()

    private val _filtroCategoria = MutableStateFlow("TODOS")
    val filtroCategoria: StateFlow<String> = _filtroCategoria.asStateFlow()

    private val _isScanningTicket = MutableStateFlow(false)
    val isScanningTicket: StateFlow<Boolean> = _isScanningTicket.asStateFlow()

    private val _borradorEscaneado = MutableStateFlow<BorradorGastoDTO?>(null)
    val borradorEscaneado: StateFlow<BorradorGastoDTO?> = _borradorEscaneado.asStateFlow()

    private val _mensajePago = MutableStateFlow<String?>(null)
    val mensajePago: StateFlow<String?> = _mensajePago.asStateFlow()

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

    private fun cargarUsuariosCasa() {
        viewModelScope.launch {
            val token = sessionManager.fetchAuthToken() ?: return@launch
            val miId = sessionManager.fetchCurrentUserId()
            try {
                val response = repository.getPisoMiembros(token, casaId)
                if (response.isSuccessful) {
                    val miembros = response.body()?.map { UsuarioPiso(it.id, it.nombre) } ?: emptyList()
                    _usuariosDetectados.value = miembros
                    _miNombre.value = miembros.find { it.id == miId }?.nombre ?: ""
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
        _gastos.value = if (categoria == "TODOS") listaCompleta else listaCompleta.filter { it.categoria == categoria }
    }

    // --- NUEVA LÓGICA: SOPORTA DECIDIR QUIÉN PAGA ---
    fun crearGasto(
        nombre: String,
        importe: String,
        categoria: String,
        beneficiarios: List<String>,
        pagadorId: Long,
    ) {
        viewModelScope.launch {
            val token = sessionManager.fetchAuthToken() ?: return@launch
            val importeDouble = importe.toDoubleOrNull() ?: 0.0
            val aportaciones = listOf(AportacionRequest(pagadorId, importeDouble))
            val urlDelEscaner = _borradorEscaneado.value?.urlTicket

            val request =
                GastoRequest(
                    nombre = nombre,
                    importe = importeDouble,
                    categoria = categoria,
                    pagadoPorId = pagadorId,
                    aportaciones = aportaciones,
                    beneficiarios = beneficiarios,
                    urlTicket = urlDelEscaner,
                )

            try {
                if (repository.crearGasto(token, casaId, request).isSuccessful) cargarGastos()
            } catch (e: Exception) {
                Log.e("GASTOS", "Excepción creando gasto: ${e.message}")
            }
        }
    }

    fun editarGasto(
        gastoId: Long,
        nombre: String,
        importe: String,
        categoria: String,
        beneficiarios: List<String>,
        pagadorId: Long,
    ) {
        viewModelScope.launch {
            val token = sessionManager.fetchAuthToken() ?: return@launch
            val importeDouble = importe.toDoubleOrNull() ?: 0.0

            val aportaciones = listOf(AportacionRequest(pagadorId, importeDouble))
            val urlExistente = listaCompleta.find { it.id == gastoId }?.fotoTicketUrl

            val request =
                GastoRequest(
                    nombre = nombre,
                    importe = importeDouble,
                    categoria = categoria,
                    pagadoPorId = pagadorId,
                    aportaciones = aportaciones,
                    beneficiarios = beneficiarios,
                    urlTicket = urlExistente,
                )

            try {
                if (repository.editarGasto(token, casaId, gastoId, request).isSuccessful) cargarGastos()
            } catch (e: Exception) {
                Log.e("GASTOS", "Excepción editando gasto: ${e.message}")
            }
        }
    }

    fun calcularPlanPagos() {
        calcularSaldosYPlanPagos(listaCompleta)
    }

    private fun calcularSaldosYPlanPagos(lista: List<Gasto>) {
        val todosLosUsuariosNombres = _usuariosDetectados.value.map { it.nombre }
        if (todosLosUsuariosNombres.isEmpty()) return

        val balances = todosLosUsuariosNombres.associateWith { 0.0 }.toMutableMap()

        lista.forEach { gasto ->
            val beneficiariosDelGasto = gasto.beneficiarios?.takeIf { it.isNotEmpty() } ?: todosLosUsuariosNombres
            if (beneficiariosDelGasto.isEmpty()) return@forEach

            val costoPorPersona = gasto.importe / beneficiariosDelGasto.size

            beneficiariosDelGasto.forEach { b -> balances[b] = (balances[b] ?: 0.0) - costoPorPersona }

            if (!gasto.aportaciones.isNullOrEmpty()) {
                gasto.aportaciones.forEach { aportacion ->
                    balances[aportacion.nombre] = (balances[aportacion.nombre] ?: 0.0) + aportacion.cantidad
                }
            } else if (gasto.pagadoPorNombre != null) {
                balances[gasto.pagadoPorNombre] = (balances[gasto.pagadoPorNombre] ?: 0.0) + gasto.importe
            }
        }

        val saldosCalculados =
            balances
                .map { (nombre, balance) ->
                    SaldoUsuario(nombre, balance, getColorPorNombreDinamico(nombre))
                }.sortedByDescending { it.cantidad }

        _saldos.value = saldosCalculados

        val deudores = saldosCalculados.filter { it.cantidad < -0.01 }.map { it.copy() }.toMutableList()
        val acreedores = saldosCalculados.filter { it.cantidad > 0.01 }.map { it.copy() }.toMutableList()

        val nuevoPlan = mutableListOf<Deuda>()
        var d = 0
        var a = 0

        while (d < deudores.size && a < acreedores.size) {
            val deudor = deudores[d]
            val acreedor = acreedores[a]

            val deudaAPagar = minOf(abs(deudor.cantidad), acreedor.cantidad)
            if (deudaAPagar > 0.01) nuevoPlan.add(Deuda(deudor.nombre, acreedor.nombre, deudaAPagar))

            deudores[d] = deudor.copy(cantidad = deudor.cantidad + deudaAPagar)
            acreedores[a] = acreedor.copy(cantidad = acreedor.cantidad - deudaAPagar)

            if (abs(deudores[d].cantidad) < 0.01) d++
            if (acreedores[a].cantidad < 0.01) a++
        }
        _planDePagos.value = nuevoPlan
    }

    fun calcularEstadisticas(lista: List<Gasto> = listaCompleta) {
        val total = lista.sumOf { it.importe }
        if (total == 0.0) {
            _stats.value = emptyList()
            return
        }

        // Recolectar lo que ha pagado cada persona en toda la historia (incluyendo gastos a medias)
        val pagosPorPersona = mutableMapOf<String, Double>()
        lista.forEach { gasto ->
            if (!gasto.aportaciones.isNullOrEmpty()) {
                gasto.aportaciones.forEach { ap ->
                    pagosPorPersona[ap.nombre] = (pagosPorPersona[ap.nombre] ?: 0.0) + ap.cantidad
                }
            } else if (gasto.pagadoPorNombre != null) {
                pagosPorPersona[gasto.pagadoPorNombre] = (pagosPorPersona[gasto.pagadoPorNombre] ?: 0.0) + gasto.importe
            } else {
                pagosPorPersona["Varios"] = (pagosPorPersona["Varios"] ?: 0.0) + gasto.importe
            }
        }

        val datosOrdenados =
            pagosPorPersona
                .map { (nombre, pagado) ->
                    Pair(nombre, (pagado / total).toFloat())
                }.sortedByDescending { it.second }

        _stats.value =
            datosOrdenados.map { (nombre, porcentaje) ->
                PieChartData(nombre, porcentaje, "${(porcentaje * 100).toInt()}%", getColorPorNombreDinamico(nombre))
            }
    }

    fun calcularSaldos(lista: List<Gasto> = listaCompleta) {
        calcularSaldosYPlanPagos(lista)
    }

    fun obtenerParticipantesGasto(gasto: Gasto): List<ParticipantePago> {
        val beneficiariosDelGasto = gasto.beneficiarios?.takeIf { it.isNotEmpty() } ?: _usuariosDetectados.value.map { it.nombre }
        if (beneficiariosDelGasto.isEmpty()) return emptyList()

        val cuota = gasto.importe / beneficiariosDelGasto.size
        return beneficiariosDelGasto.map { nombre ->
            ParticipantePago(nombre, cuota, getColorPorNombreDinamico(nombre))
        }
    }

    fun getColorPorNombreDinamico(nombre: String): Color {
        if (nombre.isEmpty() || nombre == "Desconocido") return Color.Gray
        return colorPalette[abs(nombre.hashCode()) % colorPalette.size]
    }

    fun escanearTicketIA(
        context: android.content.Context,
        uri: android.net.Uri,
    ) {
        viewModelScope.launch {
            _isScanningTicket.value = true
            try {
                val token = sessionManager.fetchAuthToken() ?: throw Exception("Sin token")
                val file = uriToFile(context, uri) ?: throw Exception("Error de imagen")
                val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

                val res = NetworkModule.casaApiService.escanearTicket(if (token.startsWith("Bearer ")) token else "Bearer $token", body)
                if (res.isSuccessful && res.body() != null) _borradorEscaneado.value = res.body()
            } catch (e: Exception) {
                Log.e("GASTOS", "Excepción IA: ${e.message}")
            } finally {
                _isScanningTicket.value = false
            }
        }
    }

    fun limpiarBorrador() {
        _borradorEscaneado.value = null
    }

    fun realizarPago(
        acreedorId: Long,
        cantidad: Double,
        gastoId: Long? = null,
    ) {
        viewModelScope.launch {
            try {
                val req = PagoBizumRequest(sessionManager.fetchCurrentUserId(), acreedorId, abs(cantidad), gastoId)
                if (repository.notificarPagoBizum(sessionManager.fetchAuthToken() ?: "", casaId, req).isSuccessful) {
                    _mensajePago.value = "Pago notificado. Esperando confirmación del receptor."
                    cargarGastos()
                }
            } catch (e: Exception) {
                _mensajePago.value = "Error de red al procesar el pago"
            }
        }
    }

    fun subirFotoTicket(
        context: Context,
        gastoId: Long,
        uri: Uri,
    ) {
        viewModelScope.launch {
            val token = sessionManager.fetchAuthToken() ?: return@launch
            val tokenFormateado = if (token.startsWith("Bearer ")) token else "Bearer $token"

            val file = uriToFile(context, uri) ?: return@launch
            val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

            val response = repository.subirFotoTicket(tokenFormateado, casaId, gastoId, body)

            if (response.isSuccessful) {
                cargarGastos()
            }
        }
    }

    fun eliminarFotoTicket(gastoId: Long) {
        viewModelScope.launch {
            try {
                val token = sessionManager.fetchAuthToken() ?: return@launch
                val response = repository.eliminarFotoTicket(token, casaId, gastoId)
                if (response.isSuccessful) {
                    cargarGastos()
                }
            } catch (e: Exception) {
                Log.e("GASTOS", "Error al borrar ticket: ${e.message}")
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
