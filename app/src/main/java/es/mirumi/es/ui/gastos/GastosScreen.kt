package es.mirumi.es.ui.gastos

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import es.mirumi.es.data.SessionManager
import es.mirumi.es.data.remote.resolveImageUrl
import es.mirumi.es.model.Gasto
import es.mirumi.es.ui.pizarra.postits.DraggableViewModel
import es.mirumi.es.ui.pizarra.postits.DraggableViewModelFactory
import es.mirumi.es.ui.pizarra.postits.PizarraScreen
import es.mirumi.es.ui.utils.BizumUtils
import es.mirumi.es.ui.utils.FabActionItem
import es.mirumi.es.ui.utils.FabActionType
import es.mirumi.es.utils.Deuda
import android.content.ContextWrapper
import androidx.activity.ComponentActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import es.mirumi.es.ui.theme.ButtonShape
import es.mirumi.es.ui.theme.CardShape
import es.mirumi.es.ui.theme.Fondo
import es.mirumi.es.ui.theme.LilaCard
import es.mirumi.es.ui.theme.LilaDark
import es.mirumi.es.ui.theme.LilaLight
import es.mirumi.es.ui.theme.RojoSaldo
import es.mirumi.es.ui.theme.TextoGris
import es.mirumi.es.ui.theme.VerdeSaldo
import es.mirumi.es.ui.utils.LogrosGlobalViewModel
import es.mirumi.es.ui.utils.LogrosGlobalViewModelFactory

// Backward-compat aliases — all downstream screens still import these names.
// The actual hex values live in `ui/theme/AppColors.kt`; this file only re-exports.
val ColorFondo = Fondo
val ColorLila = LilaLight
val ColorLilaClaroTarjeta = LilaCard
val ColorLilaSelected = LilaLight
val ColorVerdeSaldo = VerdeSaldo
val ColorRojoSaldo = RojoSaldo
val ColorTextoGris = TextoGris
val ColorMoradoOscuro = LilaDark

fun Context.getActivity(): ComponentActivity? =
    when (this) {
        is ComponentActivity -> this
        is ContextWrapper -> baseContext.getActivity()
        else -> null
    }

@Composable
fun GastosScreen(
    viewModel: GastosViewModel,
    casaId: Long,
    sessionManager: SessionManager,
) {
    val gastos by viewModel.gastos.collectAsState()
    val stats by viewModel.stats.collectAsState()
    val saldos by viewModel.saldos.collectAsState()
    val planDePagos by viewModel.planDePagos.collectAsState()
    val mostrarEstadisticas by viewModel.mostrarEstadisticas.collectAsState()
    val filtroActual by viewModel.filtroCategoria.collectAsState()
    val usuariosCasa by viewModel.usuariosDetectados.collectAsState()

    val miNombre by viewModel.miNombre.collectAsState()

    val isScanning by viewModel.isScanningTicket.collectAsState()
    val borrador by viewModel.borradorEscaneado.collectAsState()
    val context = LocalContext.current

    val activity =
        remember(context) {
            var ctx = context
            while (ctx is ContextWrapper) {
                if (ctx is ComponentActivity) break
                ctx = ctx.baseContext
            }
            ctx as? ComponentActivity
        }

    val logrosGlobalViewModel: LogrosGlobalViewModel? =
        activity?.let {
            viewModel(
                viewModelStoreOwner = it,
                factory = remember { LogrosGlobalViewModelFactory(sessionManager) },
            )
        }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer =
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    viewModel.cargarUsuariosCasa()
                    viewModel.cargarGastos()
                }
            }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    var showSourceDialog by remember { mutableStateOf(false) }
    var tempImageUri by remember { mutableStateOf<Uri?>(null) }

    val photoPickerLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia(),
        ) { uri: Uri? -> if (uri != null) viewModel.escanearTicketIA(context, uri) }

    val cameraLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.TakePicture(),
        ) { success ->
            if (success && tempImageUri != null) {
                viewModel.escanearTicketIA(context, tempImageUri!!)
            }
        }

    if (borrador != null) {
        NuevoGastoDialog(
            onDismiss = { viewModel.limpiarBorrador() },
            borradorInicial = borrador,
            miembrosCasa = usuariosCasa,
            onConfirm = { nombre, importe, categoria, beneficiarios, pagadoresMap ->
                viewModel.crearGasto(nombre, importe, categoria, beneficiarios, pagadoresMap)
                viewModel.limpiarBorrador()
            },
        )
    }

    var gastoSeleccionado by remember { mutableStateOf<Gasto?>(null) }
    var tabSeleccionado by remember { mutableIntStateOf(0) }

    var showDialog by remember { mutableStateOf(false) }
    var showDebtDialog by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(false) }

    Scaffold(containerColor = ColorFondo) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (mostrarEstadisticas) {
                VistaEstadisticas(
                    viewModel = viewModel,
                    onBack = { viewModel.toggleVista(false) },
                )
            } else if (gastoSeleccionado != null) {
                VistaDetalleGasto(
                    gasto = gastoSeleccionado!!,
                    viewModel = viewModel,
                    miNombre = miNombre,
                    onBack = { gastoSeleccionado = null },
                    onEdit = {
                        isEditing = true
                        showDialog = true
                    },
                    onDelete = { gastoSeleccionado = null },
                )
            } else {
                Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Gastos", fontSize = 32.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "Estadísticas",
                            fontSize = 14.sp,
                            textDecoration = TextDecoration.Underline,
                            modifier = Modifier.clickable { viewModel.toggleVista(true) },
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                        Box(
                            modifier =
                                Modifier
                                    .width(
                                        180.dp,
                                    ).background(Color.White, RoundedCornerShape(50))
                                    .padding(4.dp),
                        ) {
                            Row {
                                Box(
                                    modifier =
                                        Modifier
                                            .weight(1f)
                                            .height(30.dp)
                                            .background(
                                                if (tabSeleccionado ==
                                                    0
                                                ) {
                                                    ColorLilaSelected
                                                } else {
                                                    Color.Transparent
                                                },
                                                RoundedCornerShape(50),
                                            ).clickable { tabSeleccionado = 0 },
                                    contentAlignment = Alignment.Center,
                                ) { Text("Gastos", fontWeight = FontWeight.Medium, fontSize = 14.sp) }

                                Box(
                                    modifier =
                                        Modifier
                                            .weight(1f)
                                            .height(30.dp)
                                            .background(
                                                if (tabSeleccionado ==
                                                    1
                                                ) {
                                                    ColorLilaSelected
                                                } else {
                                                    Color.Transparent
                                                },
                                                RoundedCornerShape(50),
                                            ).clickable { tabSeleccionado = 1 },
                                    contentAlignment = Alignment.Center,
                                ) { Text("Saldos", fontWeight = FontWeight.Medium, fontSize = 14.sp) }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    if (tabSeleccionado == 0) {
                        VistaListaGastosContent(
                            gastos = gastos,
                            filtroActual = filtroActual,
                            onFiltrar = { viewModel.aplicarFiltro(it) },
                            onGastoClick = { gasto -> gastoSeleccionado = gasto },
                            viewModel = viewModel,
                        )
                    } else {
                        VistaSaldosContent(
                            saldos = saldos,
                            onSaldarDeudasClick = {
                                viewModel.calcularPlanPagos()
                                showDebtDialog = true
                            },
                        )
                    }
                }
            }

            if (showDialog) {
                NuevoGastoDialog(
                    onDismiss = {
                        showDialog = false
                        isEditing = false
                    },
                    gastoEditar = if (isEditing) gastoSeleccionado else null,
                    miembrosCasa = usuariosCasa,
                    // 🔴 Cambiamos pagadorIdSelected por pagadoresMap
                    onConfirm = { nombre, importe, categoria, beneficiarios, pagadoresMap ->
                        if (isEditing && gastoSeleccionado != null) {
                            viewModel.editarGasto(
                                gastoSeleccionado!!.id,
                                nombre,
                                importe,
                                categoria,
                                beneficiarios,
                                pagadoresMap,
                            )
                        } else {
                            viewModel.crearGasto(nombre, importe, categoria, beneficiarios, pagadoresMap) {
                                logrosGlobalViewModel?.comprobarNuevosLogros(silencioso = false)
                            }
                        }
                        showDialog = false
                        if (isEditing) gastoSeleccionado = null
                        isEditing = false
                    },
                )
            }

            if (showDebtDialog) {
                DialogPlanPagos(
                    plan = planDePagos,
                    onDismiss = { showDebtDialog = false },
                    viewModel = viewModel,
                    miNombre = miNombre,
                )
            }

            if (isScanning) {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Color.White)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Analizando ticket...", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (showSourceDialog) {
                AlertDialog(
                    onDismissRequest = { showSourceDialog = false },
                    title = { Text("Escanear Ticket") },
                    text = { Text("¿Desde dónde quieres obtener la foto del ticket?") },
                    containerColor = Color.White,
                    confirmButton = {
                        Button(
                            onClick = {
                                showSourceDialog = false
                                val uri = viewModel.createTempUri(context)
                                tempImageUri = uri
                                cameraLauncher.launch(uri)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ColorLila),
                        ) {
                            Text("Cámara", color = ColorMoradoOscuro)
                        }
                    },
                    dismissButton = {
                        OutlinedButton(
                            onClick = {
                                showSourceDialog = false
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                                )
                            },
                        ) {
                            Text("Galería", color = ColorMoradoOscuro)
                        }
                    },
                )
            }
        }
    }

    if (gastoSeleccionado == null) {
        val pizarraFabActions =
            listOf(
                FabActionItem(icon = Icons.Default.NoteAdd, label = "Crear Post-it", action = FabActionType.POST_IT),
                FabActionItem(
                    icon = Icons.Default.Add,
                    label = "Crear Gasto Manual",
                    action = FabActionType.CREAR_GASTO,
                ),
                FabActionItem(
                    icon = Icons.Default.DocumentScanner,
                    label = "Escanear Ticket",
                    action = FabActionType.ESCANEAR_TICKET,
                ),
            )

        val keyStr =
            if (mostrarEstadisticas) {
                "Gastos Estadisticas"
            } else if (tabSeleccionado == 0) {
                "Gastos"
            } else {
                "Gastos Saldo"
            }

        val model =
            viewModel<DraggableViewModel>(
                key = keyStr,
                factory = DraggableViewModelFactory(keyStr, casaId, sessionManager),
            )

        PizarraScreen(
            model,
            fabActions = pizarraFabActions,
            onFabActionSelected = { action ->
                when (action.action) {
                    FabActionType.POST_IT -> model.addNewPostIt()
                    FabActionType.CREAR_GASTO -> {
                        isEditing = false
                        showDialog = true
                    }
                    FabActionType.ESCANEAR_TICKET -> {
                        showSourceDialog = true
                    }
                    else -> {}
                }
            },
        )
    }
}

// COMPONENTES SECUNDARIOS (Listas, Tarjetas, Diálogos)
@Composable
fun VistaListaGastosContent(
    gastos: List<Gasto>,
    filtroActual: String,
    onFiltrar: (String) -> Unit,
    onGastoClick: (Gasto) -> Unit,
    viewModel: GastosViewModel,
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) {
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val categorias = listOf("TODOS", "ALQUILER", "COMIDA", "SUMINISTROS", "OCIO", "OTROS")
                items(categorias) { cat -> CategoryChip(cat, filtroActual, onFiltrar) }
            }
        }
        item { Spacer(modifier = Modifier.height(16.dp)) }
        if (gastos.isEmpty()) {
            item { Text("No hay gastos registrados.", color = Color.Gray) }
        } else {
            items(gastos) { gasto -> ItemGasto(gasto, viewModel, onClick = { onGastoClick(gasto) }) }
        }
        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
fun VistaSaldosContent(
    saldos: List<SaldoUsuario>,
    onSaldarDeudasClick: () -> Unit,
) {
    Column {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = CardShape,
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(40.dp).background(Color(0xFFC8E6C9), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center,
                ) { Icon(Icons.Default.SentimentSatisfiedAlt, null, tint = Color(0xFF2E7D32)) }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Balance del Grupo", fontWeight = FontWeight.Bold)
                    Text("Saldos calculados equitativamente", fontSize = 12.sp, color = ColorTextoGris)
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onSaldarDeudasClick,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ColorLila),
            shape = ButtonShape,
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp, pressedElevation = 1.dp),
        ) {
            Icon(Icons.Default.Calculate, contentDescription = null, tint = ColorMoradoOscuro)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Saldar Deudas", color = ColorMoradoOscuro, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text("BALANCES", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ColorTextoGris)
        Spacer(modifier = Modifier.height(12.dp))

        if (saldos.isEmpty()) {
            Text("No hay datos de saldos suficientes.", color = Color.Gray)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(saldos) { saldo -> ItemSaldo(saldo) }
            }
        }
    }
}

@Composable
fun ItemSaldo(saldo: SaldoUsuario) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = CardShape,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AvatarConFoto(saldo.nombre, saldo.colorAvatar, saldo.fotoUrl)
                Spacer(modifier = Modifier.width(12.dp))
                Text(saldo.nombre, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }
            val signo = if (saldo.cantidad >= 0) "+" else ""
            val colorTexto = if (saldo.cantidad >= 0) ColorVerdeSaldo else ColorRojoSaldo
            Text(
                text = "$signo${String.format("%.2f", saldo.cantidad).replace('.', ',')}€",
                color = colorTexto,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
            )
        }
    }
}

@Composable
fun DialogPlanPagos(
    plan: List<Deuda>,
    onDismiss: () -> Unit,
    viewModel: GastosViewModel,
    miNombre: String,
) {
    val context = LocalContext.current

    val activity = context.getActivity()
    val logrosGlobalViewModel: LogrosGlobalViewModel? =
        activity?.let {
            viewModel(
                viewModelStoreOwner = it,
                factory = remember { LogrosGlobalViewModelFactory(SessionManager(context)) },
            )
        }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Plan de Pagos", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (plan.isEmpty()) {
                    Text("¡No hay deudas pendientes! Todo está saldado.", color = ColorVerdeSaldo)
                } else {
                    Text("Para ajustar cuentas, haced estos pagos:", fontSize = 14.sp, color = ColorTextoGris)
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(plan) { deuda ->
                            val soyElQueDebe = deuda.de == miNombre
                            val soyElQueRecibe = deuda.para == miNombre

                            Card(
                                colors = CardDefaults.cardColors(containerColor = ColorFondo),
                                shape = RoundedCornerShape(8.dp),
                            ) {
                                Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            AvatarConFoto(
                                                deuda.de,
                                                viewModel.getColorPorNombreDinamico(deuda.de),
                                                viewModel.getFotoPorNombre(deuda.de),
                                                30.dp,
                                            )
                                            Icon(
                                                Icons.Default.ArrowForward,
                                                contentDescription = "Paga a",
                                                modifier = Modifier.padding(horizontal = 8.dp).size(16.dp),
                                                tint = ColorTextoGris,
                                            )
                                            AvatarConFoto(
                                                deuda.para,
                                                viewModel.getColorPorNombreDinamico(deuda.para),
                                                viewModel.getFotoPorNombre(deuda.para),
                                                30.dp,
                                            )
                                        }
                                        Text(
                                            "${String.format("%.2f", deuda.cantidad)}€",
                                            fontWeight = FontWeight.Bold,
                                            color = ColorMoradoOscuro,
                                        )
                                    }

                                    // LÓGICA BIZUM EN DEUDAS GLOBALES
                                    if (soyElQueDebe || soyElQueRecibe) {
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.End,
                                        ) {
                                            if (soyElQueDebe) {
                                                Box(
                                                    modifier =
                                                        Modifier
                                                            .background(Color(0xFF00C4B3), RoundedCornerShape(8.dp))
                                                            .clickable {
                                                                val acreedor =
                                                                    viewModel.usuariosDetectados.value.find {
                                                                        it.nombre ==
                                                                            deuda.para
                                                                    }
                                                                if (acreedor != null) {
                                                                    BizumUtils.abrirAppBancariaParaBizum(
                                                                        context,
                                                                        null,
                                                                        deuda.cantidad,
                                                                    )
                                                                    viewModel.realizarPago(
                                                                        acreedorId = acreedor.id,
                                                                        cantidad = deuda.cantidad,
                                                                    ) {
                                                                        logrosGlobalViewModel?.comprobarNuevosLogros(
                                                                            silencioso = false,
                                                                        )
                                                                    }
                                                                }
                                                            }.padding(horizontal = 12.dp, vertical = 8.dp),
                                                ) {
                                                    Text(
                                                        "Pagar con Bizum",
                                                        color = Color.White,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                    )
                                                }
                                            } else if (soyElQueRecibe) {
                                                Box(
                                                    modifier =
                                                        Modifier
                                                            .background(Color(0xFF2196F3), RoundedCornerShape(8.dp))
                                                            .clickable {
                                                                BizumUtils.solicitarBizum(context, null, deuda.cantidad)
                                                            }.padding(horizontal = 12.dp, vertical = 8.dp),
                                                ) {
                                                    Text(
                                                        "Solicitar Bizum",
                                                        color = Color.White,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Entendido", color = ColorMoradoOscuro) } },
        containerColor = Color.White,
    )
}

// VISTAS DETALLADAS DE GASTOS Y PARTICIPANTES
@Composable
fun ItemGasto(
    gasto: Gasto,
    viewModel: GastosViewModel,
    onClick: () -> Unit,
) {
    // Observe the members map so the row recomposes the moment `_usuariosDetectados` populates.
    // Without this, `getFotoPorNombre` reads a stale empty snapshot on the initial render,
    // and the avatar only appears on the next re-entry (navigation back from detail).
    val usuarios by viewModel.usuariosDetectados.collectAsState()

    val pagadorNombre =
        if (!gasto.aportaciones.isNullOrEmpty()) {
            if (gasto.aportaciones.size > 1) "Varios" else gasto.aportaciones.first().nombre
        } else if (!gasto.pagadoPorNombre.isNullOrEmpty() && gasto.pagadoPorNombre != "Desconocido") {
            gasto.pagadoPorNombre
        } else {
            null
        }

    Card(
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        // Premium elevation — lifts cards off the `Fondo` background and gives them depth.
        // Matches the elevation rhythm used on TareasScreen's task cards.
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp, pressedElevation = 4.dp),
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
    ) {
        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.Top) {
            Icon(
                imageVector = getIconoCategoria(gasto.categoria),
                contentDescription = null,
                modifier = Modifier.padding(top = 4.dp, end = 16.dp).size(24.dp),
                tint = Color.Black,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(gasto.nombre, fontSize = 18.sp, fontWeight = FontWeight.W600)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (pagadorNombre != null) {
                        val colorAvatar = viewModel.getColorPorNombreDinamico(pagadorNombre)
                        // Derive from observed `usuarios` state so Compose tracks the read and
                        // recomposes as soon as the members map populates.
                        val fotoAvatar = usuarios.find { it.nombre == pagadorNombre }?.fotoUrl

                        AvatarConFoto(pagadorNombre, colorAvatar, fotoAvatar, 24.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Pagado por: $pagadorNombre", fontSize = 13.sp, color = ColorTextoGris)
                    } else {
                        Icon(
                            Icons.Default.Calculate,
                            contentDescription = null,
                            tint = ColorRojoSaldo,
                            modifier = Modifier.size(24.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Falta por pagar", fontSize = 13.sp, color = ColorRojoSaldo, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("${gasto.importe.toInt()}€", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun VistaDetalleGasto(
    gasto: Gasto,
    viewModel: GastosViewModel,
    miNombre: String,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val pagadorNombre =
        if (!gasto.aportaciones.isNullOrEmpty()) {
            if (gasto.aportaciones.size > 1) "Varios (A partes iguales)" else gasto.aportaciones.first().nombre
        } else if (!gasto.pagadoPorNombre.isNullOrEmpty() && gasto.pagadoPorNombre != "Desconocido") {
            gasto.pagadoPorNombre
        } else {
            null
        }

    val participantes = viewModel.obtenerParticipantesGasto(gasto)

    Column(modifier = Modifier.fillMaxSize().background(ColorFondo)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver") }
            IconButton(onClick = onDelete) { Icon(Icons.Default.DeleteOutline, contentDescription = "Borrar") }
        }

        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(gasto.nombre, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text(gasto.fecha.take(10), fontSize = 14.sp, color = ColorTextoGris)
        }
        Spacer(modifier = Modifier.height(30.dp))

        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Text(
                if (pagadorNombre !=
                    null
                ) {
                    "PAGADO POR"
                } else {
                    "ESTADO DEL GASTO"
                },
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = ColorTextoGris,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = CardShape,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (pagadorNombre != null) {
                            val colorAvatar = viewModel.getColorPorNombreDinamico(pagadorNombre)
                            val fotoAvatar = viewModel.getFotoPorNombre(pagadorNombre)

                            AvatarConFoto(pagadorNombre, colorAvatar, fotoAvatar, 36.dp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(pagadorNombre, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        } else {
                            Box(
                                modifier = Modifier.size(36.dp).background(Color(0xFFFFEBEE), CircleShape),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(Icons.Default.Calculate, contentDescription = null, tint = ColorRojoSaldo)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "Falta por pagar",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = ColorRojoSaldo,
                            )
                        }
                    }
                    Text(
                        "${gasto.importe}€",
                        fontSize = 16.sp,
                        color =
                            if (pagadorNombre !=
                                null
                            ) {
                                ColorVerdeSaldo
                            } else {
                                ColorRojoSaldo
                            },
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Column(modifier = Modifier.padding(horizontal = 20.dp).weight(1f)) {
            Text(
                if (pagadorNombre !=
                    null
                ) {
                    "FALTA POR PAGAR (PARTICIPANTES)"
                } else {
                    "REPARTO DEL GASTO"
                },
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = ColorTextoGris,
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(participantes) { part ->
                    ItemParticipante(part, pagadorNombre ?: "Nadie", gasto, viewModel, miNombre)
                }
            }
        }

        Box(modifier = Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
            OutlinedButton(
                onClick = onEdit,
                modifier = Modifier.width(200.dp),
                shape = ButtonShape,
                border = BorderStroke(1.dp, Color.Gray),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ColorTextoGris),
            ) {
                Text("Modificar")
            }
        }
    }
}

@Composable
fun ItemParticipante(
    part: ParticipantePago,
    pagadorPrincipal: String,
    gasto: Gasto,
    viewModel: GastosViewModel,
    miNombre: String,
) {
    val context = LocalContext.current

    val activity = context.getActivity()
    val logrosGlobalViewModel: LogrosGlobalViewModel? =
        activity?.let {
            viewModel(
                viewModelStoreOwner = it,
                factory = remember { LogrosGlobalViewModelFactory(SessionManager(context)) },
            )
        }

    val esElPagadorDeLaFila = part.nombre == pagadorPrincipal
    val soyElPagadorPrincipal = miNombre == pagadorPrincipal
    val soyEsteParticipante = miNombre == part.nombre

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = CardShape,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AvatarConFoto(part.nombre, part.colorAvatar, part.fotoUrl, 36.dp)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(part.nombre, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    if (esElPagadorDeLaFila) Text("Ya pagado", fontSize = 12.sp, color = ColorVerdeSaldo)
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "${String.format("%.2f", part.cantidad).replace('.', ',')}€",
                    fontSize = 16.sp,
                    color = if (esElPagadorDeLaFila) ColorTextoGris else ColorRojoSaldo,
                    fontWeight = FontWeight.Bold,
                )

                if (!esElPagadorDeLaFila && !pagadorPrincipal.startsWith("Varios") && pagadorPrincipal != "Nadie") {
                    Spacer(modifier = Modifier.width(12.dp))

                    if (soyElPagadorPrincipal) {
                        Box(
                            modifier =
                                Modifier
                                    .background(Color(0xFF2196F3), RoundedCornerShape(8.dp))
                                    .clickable {
                                        BizumUtils.solicitarBizum(context, null, part.cantidad)
                                    }.padding(horizontal = 8.dp, vertical = 6.dp),
                        ) { Text("Solicitar", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    } else if (soyEsteParticipante) {
                        Box(
                            modifier =
                                Modifier
                                    .background(Color(0xFF00C4B3), RoundedCornerShape(8.dp))
                                    .clickable {
                                        val acreedor =
                                            viewModel.usuariosDetectados.value.find {
                                                it.nombre ==
                                                    pagadorPrincipal
                                            }
                                        if (acreedor != null) {
                                            BizumUtils.abrirAppBancariaParaBizum(context, null, part.cantidad)

                                            viewModel.realizarPago(
                                                acreedorId = acreedor.id,
                                                cantidad = part.cantidad,
                                                gastoId = gasto.id,
                                            ) {
                                                logrosGlobalViewModel?.comprobarNuevosLogros(silencioso = false)
                                            }
                                        }
                                    }.padding(horizontal = 8.dp, vertical = 6.dp),
                        ) { Text("Bizum", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    } else {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "Recordar",
                            tint = Color(0xFFFFB300),
                            modifier =
                                Modifier.size(28.dp).clickable {
                                    Toast
                                        .makeText(
                                            context,
                                            "Recordatorio enviado a ${part.nombre}",
                                            Toast.LENGTH_SHORT,
                                        ).show()
                                },
                        )
                    }
                }
            }
        }
    }
}

// UTILIDADES GRÁFICAS (Avatar y Estadísticas)
@Composable
fun AvatarConFoto(
    nombre: String,
    colorFondo: Color,
    fotoUrl: String?,
    size: Dp = 40.dp,
) {
    val inicial = nombre.firstOrNull()?.toString()?.uppercase() ?: "?"
    val resolvedFotoUrl = resolveImageUrl(fotoUrl)
    val shouldRenderPhoto = resolvedFotoUrl != null && !nombre.startsWith("Varios")

    Box(modifier = Modifier.size(size).clip(CircleShape).background(colorFondo), contentAlignment = Alignment.Center) {
        Text(
            text = inicial,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize =
                if (size <
                    30.dp
                ) {
                    12.sp
                } else {
                    16.sp
                },
        )
        if (shouldRenderPhoto) {
            AsyncImage(
                model =
                    ImageRequest
                        .Builder(LocalContext.current)
                        .data(resolvedFotoUrl)
                        .crossfade(true)
                        .build(),
                contentDescription = "Foto",
                contentScale = ContentScale.Crop,
                modifier =
                    Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
            )
        }
    }
}

@Composable
fun AvatarConInicial(
    nombre: String,
    colorFondo: Color,
    size: Dp = 40.dp,
) {
    AvatarConFoto(nombre, colorFondo, null, size)
}

@Composable
fun VistaEstadisticas(
    viewModel: GastosViewModel,
    onBack: () -> Unit,
) {
    val context = LocalContext.current

    // Recogemos ambas listas del ViewModel
    val statsPorPersona by viewModel.statsPorPersona.collectAsState()
    val statsPorCategoria by viewModel.statsPorCategoria.collectAsState()

    // Estado para controlar qué página vemos (0 = Persona, 1 = Categoría)
    var paginaActual by remember { mutableIntStateOf(0) }

    // Control del Diálogo del PDF
    var showPdfDialog by remember { mutableStateOf(false) }

    // Elegimos qué datos mostrar según la página
    val statsActuales = if (paginaActual == 0) statsPorPersona else statsPorCategoria
    val tituloActual = if (paginaActual == 0) "¿Cuánto\nhas pagado?" else "¿En qué\ngastamos más?"

    Column(modifier = Modifier.fillMaxSize().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        // Botón de Volver
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver") }
            Spacer(modifier = Modifier.weight(1f))
            Text("Estadísticas", color = ColorTextoGris, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(10.dp))

        // CARRUSEL CON FLECHAS Y TÍTULO DINÁMICO
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = { paginaActual = 0 },
                enabled = paginaActual > 0,
            ) {
                Icon(
                    Icons.Default.ArrowBackIosNew,
                    contentDescription = "Atrás",
                    tint =
                        if (paginaActual >
                            0
                        ) {
                            Color.Black
                        } else {
                            Color.Transparent
                        },
                )
            }

            Text(
                text = tituloActual,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 34.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
            )

            IconButton(
                onClick = { paginaActual = 1 },
                enabled = paginaActual < 1,
            ) {
                Icon(
                    Icons.Default.ArrowForwardIos,
                    contentDescription = "Adelante",
                    tint =
                        if (paginaActual <
                            1
                        ) {
                            Color.Black
                        } else {
                            Color.Transparent
                        },
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        // BURBUJAS INTERACTIVAS
        Box(modifier = Modifier.size(320.dp)) {
            if (statsActuales.isEmpty()) {
                Text("Sin datos", modifier = Modifier.align(Alignment.Center))
            } else {
                statsActuales.take(5).forEachIndexed { index, data ->

                    val onClickBurbuja = {
                        if (paginaActual == 1) {
                            // Si estamos en Categorías, filtramos la lista y cerramos las stats
                            viewModel.aplicarFiltro(data.categoria)
                            onBack()
                        } else {
                            // Opcional: mostrar un Toast al pulsar en una persona
                            Toast.makeText(context, "Pagado por: ${data.categoria}", Toast.LENGTH_SHORT).show()
                        }
                    }

                    when (index) {
                        0 ->
                            BubbleShape(
                                text = data.textoPorcentaje,
                                color = data.color,
                                size = 200.dp,
                                shape = RoundedCornerShape(40),
                                rotation = 10f,
                                offset = DpOffset(10.dp, 10.dp),
                                modifier = Modifier.align(Alignment.TopStart),
                                onClick = onClickBurbuja,
                            )
                        1 ->
                            BubbleShape(
                                text = data.textoPorcentaje,
                                color = data.color,
                                size = 150.dp,
                                shape = CircleShape,
                                rotation = 0f,
                                offset = DpOffset(20.dp, (-10).dp),
                                modifier = Modifier.align(Alignment.BottomStart),
                                onClick = onClickBurbuja,
                            )
                        2 ->
                            BubbleShape(
                                text = data.textoPorcentaje,
                                color = data.color,
                                size = 130.dp,
                                shape = RoundedCornerShape(30.dp),
                                rotation = -25f,
                                offset = DpOffset((-10).dp, 60.dp),
                                modifier = Modifier.align(Alignment.TopEnd),
                                onClick = onClickBurbuja,
                            )
                        3 ->
                            BubbleShape(
                                text = data.textoPorcentaje,
                                color = data.color,
                                width = 120.dp,
                                height = 70.dp,
                                shape = RoundedCornerShape(20.dp),
                                rotation = 5f,
                                offset = DpOffset(0.dp, 0.dp),
                                modifier = Modifier.align(Alignment.BottomEnd),
                                onClick = onClickBurbuja,
                            )
                        4 ->
                            BubbleShape(
                                text = data.textoPorcentaje,
                                color = data.color,
                                size = 90.dp,
                                shape = CircleShape,
                                rotation = -10f,
                                offset = DpOffset(120.dp, 120.dp),
                                modifier = Modifier.align(Alignment.Center),
                                onClick = onClickBurbuja,
                            )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // LEYENDA
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            statsActuales.take(4).forEach {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(12.dp).background(it.color, CircleShape))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(it.categoria, fontSize = 14.sp, color = ColorTextoGris)
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        // BOTÓN DE DESCARGAR PDF
        Button(
            onClick = { showPdfDialog = true },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ColorMoradoOscuro),
            shape = RoundedCornerShape(12.dp),
        ) {
            Icon(Icons.Default.PictureAsPdf, contentDescription = "Descargar PDF", tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Generar PDF de Cuentas", color = Color.White, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(20.dp))
    }

    // DIÁLOGO DE OPCIONES DE PDF
    if (showPdfDialog) {
        var incluirDeudas by remember { mutableStateOf(true) }

        AlertDialog(
            onDismissRequest = { showPdfDialog = false },
            title = { Text("Opciones del Informe", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Selecciona qué deseas incluir en el documento PDF:", color = Color.Gray)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier =
                            Modifier.clickable {
                                incluirDeudas =
                                    !incluirDeudas
                            },
                    ) {
                        Checkbox(checked = incluirDeudas, onCheckedChange = {
                            incluirDeudas = it
                        }, colors = CheckboxDefaults.colors(checkedColor = ColorMoradoOscuro))
                        Text("Incluir Plan de Pagos (Quién debe a quién)")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPdfDialog = false
                        viewModel.descargarPdfCuentas(context, "Piso")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ColorMoradoOscuro),
                ) {
                    Text("Descargar", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPdfDialog = false }) { Text("Cancelar", color = Color.Gray) }
            },
            containerColor = Color.White,
        )
    }
}

@Composable
fun BubbleShape(
    text: String,
    color: Color,
    size: Dp? = null,
    width: Dp? = null,
    height: Dp? = null,
    shape: androidx.compose.ui.graphics.Shape,
    rotation: Float,
    offset: DpOffset,
    modifier: Modifier,
    onClick: () -> Unit = {},
) {
    Box(
        modifier =
            modifier
                .offset(x = offset.x, y = offset.y)
                .rotate(rotation)
                .then(if (size != null) Modifier.size(size) else Modifier.size(width = width!!, height = height!!))
                .clip(shape)
                .background(color)
                .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.rotate(-rotation),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryChip(
    label: String,
    selectedCategory: String,
    onSelect: (String) -> Unit,
) {
    val isSelected = label == selectedCategory
    FilterChip(
        selected = isSelected,
        onClick = { onSelect(label) },
        label = { Text(label) },
        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = ColorLila, containerColor = ColorFondo),
        border =
            FilterChipDefaults.filterChipBorder(
                enabled = true,
                selected = isSelected,
                borderColor = if (isSelected) ColorMoradoOscuro else Color.Gray,
            ),
    )
}

fun getIconoCategoria(categoria: String): ImageVector =
    when (categoria.uppercase()) {
        "COMIDA" -> Icons.Default.RestaurantMenu
        "ALQUILER" -> Icons.Default.Home
        "SUMINISTROS" -> Icons.Default.Lightbulb
        "OCIO" -> Icons.Default.SentimentSatisfiedAlt
        "TRANSPORTE" -> Icons.Default.DirectionsBus
        else -> Icons.Default.AttachMoney
    }
