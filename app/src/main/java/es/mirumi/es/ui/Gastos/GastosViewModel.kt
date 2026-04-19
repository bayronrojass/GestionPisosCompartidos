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
import es.mirumi.es.model.requests.GastoRequest
import es.mirumi.es.utils.DebtCalculator
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

    private val _isScanningTicket = MutableStateFlow(false)
    val isScanningTicket: StateFlow<Boolean> = _isScanningTicket.asStateFlow()

    private val _borradorEscaneado = MutableStateFlow<BorradorGastoDTO?>(null)
    val borradorEscaneado: StateFlow<BorradorGastoDTO?> = _borradorEscaneado.asStateFlow()

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
        cargarUsuariosCasa() // ¡Primero cargamos los miembros oficiales!
        cargarGastos() // Luego los gastos
    }

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

    // --- CÁLCULO DE PAGOS CORREGIDO (Usa los beneficiarios de cada gasto) ---
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

                // ¡LA CLAVE! Solo usamos los beneficiarios guardados en el gasto.
                // Si la lista de la BD viene vacía por error, por defecto asume todos.
                val beneficiariosDelGasto = gasto.beneficiarios?.takeIf { it.isNotEmpty() } ?: todosLosUsuarios

                val numBeneficiarios = beneficiariosDelGasto.size
                if (numBeneficiarios == 0) return@forEach

                val importePorPersona = gasto.importe / numBeneficiarios

                beneficiariosDelGasto.forEach { usuario ->
                    if (usuario != pagador) {
                        deudasBrutas.add(Deuda(de = usuario, para = pagador, cantidad = importePorPersona))
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

    // --- CÁLCULO DE SALDOS CORREGIDO (Respeta quién participa y quién no) ---
    private fun calcularSaldos(lista: List<Gasto>) {
        val todosLosUsuarios = _usuariosDetectados.value
        if (lista.isEmpty() || todosLosUsuarios.isEmpty()) {
            _saldos.value = emptyList()
            return
        }

        // 1. Iniciamos un mapa con todos los usuarios de la casa a 0.0
        val balances = todosLosUsuarios.associateWith { 0.0 }.toMutableMap()

        // 2. Por cada gasto, sumamos el dinero a quien lo pagó, y le restamos su parte a los beneficiarios
        lista.forEach { gasto ->
            val pagador = gasto.pagadoPorNombre
            val beneficiariosDelGasto = gasto.beneficiarios?.takeIf { it.isNotEmpty() } ?: todosLosUsuarios
            val costoPorPersona = gasto.importe / beneficiariosDelGasto.size

            if (pagador != null && balances.containsKey(pagador)) {
                balances[pagador] = balances[pagador]!! + gasto.importe
            }

            beneficiariosDelGasto.forEach { beneficiario ->
                if (balances.containsKey(beneficiario)) {
                    balances[beneficiario] = balances[beneficiario]!! - costoPorPersona
                }
            }
        }

        val saldosCalculados =
            balances
                .map { (nombre, balance) ->
                    SaldoUsuario(nombre = nombre, cantidad = balance, colorAvatar = getColorPorNombreDinamico(nombre))
                }.sortedByDescending { it.cantidad }

        _saldos.value = saldosCalculados
    }

    // --- PARTICIPANTES CORREGIDO (Le pasamos el gasto entero, no solo el importe) ---
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

    fun editarGasto(
        gastoId: Long,
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
                    beneficiarios = beneficiarios,
                )

            try {
                // Asegúrate de tener este método en tu RepositoryCasa y tu ApiService (PUT /casas/{casaId}/gastos/{gastoId})
                val response = repository.editarGasto(token, casaId, gastoId, request)
                if (response.isSuccessful) {
                    cargarGastos() // Recarga la lista para mostrar los cambios
                } else {
                    Log.e("GASTOS", "Error editando gasto: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("GASTOS", "Excepción editando gasto: ${e.message}")
            }
        }
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
