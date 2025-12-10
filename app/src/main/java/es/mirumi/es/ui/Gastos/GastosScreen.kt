package es.mirumi.es.ui.gastos

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.SentimentSatisfiedAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import es.mirumi.es.model.Gasto
import es.mirumi.es.ui.pizarra.postits.DraggableViewModel
import es.mirumi.es.ui.pizarra.postits.DraggableViewModelFactory
import es.mirumi.es.ui.pizarra.postits.PizarraScreen
import es.mirumi.es.ui.utils.FabActionItem
import es.mirumi.es.ui.utils.FabActionType

val ColorFondo = Color(0xFFF8F8F8)
val ColorLila = Color(0xFFDDC1FB)
val ColorLilaClaroTarjeta = Color(0xFFE8D5FC)
val ColorLilaSelected = Color(0xFFDDC1FB)
val ColorVerdeSaldo = Color(0xFF4CAF50)
val ColorRojoSaldo = Color(0xFFE57373)
val ColorTextoGris = Color(0xFF6C6C6C)
val ColorMoradoOscuro = Color(0xFF58337F)

@Composable
fun GastosScreen(
    viewModel: GastosViewModel,
    casaId: Long,
) {
    val gastos by viewModel.gastos.collectAsState()
    val stats by viewModel.stats.collectAsState()
    val saldos by viewModel.saldos.collectAsState()
    val mostrarEstadisticas by viewModel.mostrarEstadisticas.collectAsState()
    val filtroActual by viewModel.filtroCategoria.collectAsState()

    var gastoSeleccionado by remember { mutableStateOf<Gasto?>(null) }
    var tabSeleccionado by remember { mutableIntStateOf(0) }

    var showDialog by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = ColorFondo,
    ) { padding ->
        Box(modifier = Modifier
            .padding(padding)
            .fillMaxSize()) {
            if (mostrarEstadisticas) {
                VistaEstadisticas(
                    stats = stats,
                    onBack = { viewModel.toggleVista(false) },
                )
            } else if (gastoSeleccionado != null) {
                VistaDetalleGasto(
                    gasto = gastoSeleccionado!!,
                    viewModel = viewModel,
                    onBack = { gastoSeleccionado = null },
                    onEdit = {
                        isEditing = true
                        showDialog = true
                    },
                    onDelete = {
                        gastoSeleccionado = null
                    },
                )
            } else {
                Column(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp),
                ) {
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

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Box(
                            modifier =
                                Modifier
                                    .width(180.dp)
                                    .background(Color.White, RoundedCornerShape(50))
                                    .padding(4.dp),
                        ) {
                            Row {
                                Box(
                                    modifier =
                                        Modifier
                                            .weight(1f)
                                            .height(30.dp)
                                            .background(
                                                if (tabSeleccionado == 0) ColorLilaSelected else Color.Transparent,
                                                RoundedCornerShape(50),
                                            )
                                            .clickable { tabSeleccionado = 0 },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text("Gastos", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                                }
                                Box(
                                    modifier =
                                        Modifier
                                            .weight(1f)
                                            .height(30.dp)
                                            .background(
                                                if (tabSeleccionado == 1) ColorLilaSelected else Color.Transparent,
                                                RoundedCornerShape(50),
                                            )
                                            .clickable { tabSeleccionado = 1 },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text("Saldos", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                                }
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
                        VistaSaldosContent(saldos)
                    }
                }
            }

            if (showDialog) {
                NuevoGastoDialog(
                    onDismiss = { showDialog = false },
                    gastoEditar = if (isEditing) gastoSeleccionado else null,
                    onConfirm = { nombre, importe, categoria ->
                        if (isEditing && gastoSeleccionado != null) {
                            // Lógica de editar futura
                        } else {
                            viewModel.crearGasto(nombre, importe, categoria)
                        }
                        showDialog = false
                        if (isEditing) gastoSeleccionado = null
                    },
                )
            }
        }
    }
    if (gastoSeleccionado == null) {
        val pizarraFabActions =
            listOf(
                FabActionItem(
                    icon = Icons.Default.NoteAdd,
                    label = "Crear Post-it",
                    action = FabActionType.POST_IT,
                ),
                FabActionItem(
                    icon = Icons.Default.Add,
                    label = "Crear Gasto",
                    action = FabActionType.CREAR_GASTO,
                ),
            )

        var model: DraggableViewModel
        if (mostrarEstadisticas) {
            model =
                viewModel<DraggableViewModel>(
                    key = "Gastos Estadisticas",
                    factory = DraggableViewModelFactory("Gastos Estadisticas", casaId),
                )
        } else if (tabSeleccionado == 0) {
            model =
                viewModel<DraggableViewModel>(
                    key = "Gastos",
                    factory = DraggableViewModelFactory("Gastos", casaId),
                )
        } else {
            model =
                viewModel<DraggableViewModel>(
                    key = "Gastos Saldo",
                    factory = DraggableViewModelFactory("Gastos Saldo", casaId),
                )
        }

        PizarraScreen(
            model,
            fabActions = pizarraFabActions,
            onFabActionSelected = { action ->
                when (action.action) {
                    FabActionType.POST_IT -> {
                        model.addNewPostIt()
                    }

                    FabActionType.CREAR_GASTO -> {
                        isEditing = false
                        showDialog = true
                    }

                    else -> {}
                }
            },
        )
    }
}

@Composable
fun VistaListaGastosContent(
    gastos: List<Gasto>,
    filtroActual: String,
    onFiltrar: (String) -> Unit,
    onGastoClick: (Gasto) -> Unit,
    viewModel: GastosViewModel,
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val categorias =
                    listOf("TODOS", "ALQUILER", "COMIDA", "SUMINISTROS", "OCIO", "OTROS")
                items(categorias) { cat ->
                    CategoryChip(cat, filtroActual, onFiltrar)
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "GASTOS FIJOS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = ColorTextoGris
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        item { ItemGastoFijoEjemplo() }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Recientes", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (gastos.isEmpty()) {
            item { Text("No hay gastos registrados.", color = Color.Gray) }
        } else {
            items(gastos) { gasto ->
                ItemGasto(gasto, viewModel, onClick = { onGastoClick(gasto) })
            }
        }
        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
fun VistaSaldosContent(saldos: List<SaldoUsuario>) {
    Column {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(1.dp),
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier =
                        Modifier
                            .size(40.dp)
                            .background(Color(0xFFC8E6C9), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.SentimentSatisfiedAlt, null, tint = Color(0xFF2E7D32))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Balance del Grupo", fontWeight = FontWeight.Bold)
                    Text(
                        "Saldos calculados equitativamente",
                        fontSize = 12.sp,
                        color = ColorTextoGris
                    )
                }
                Icon(Icons.Default.KeyboardArrowRight, null)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("BALANCES", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ColorTextoGris)
        Spacer(modifier = Modifier.height(12.dp))

        if (saldos.isEmpty()) {
            Text("No hay datos de saldos suficientes.", color = Color.Gray)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(saldos) { saldo ->
                    ItemSaldo(saldo)
                }
            }
        }
    }
}

@Composable
fun ItemSaldo(saldo: SaldoUsuario) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(0.dp),
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AvatarConInicial(saldo.nombre, saldo.colorAvatar)
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
fun ItemGasto(
    gasto: Gasto,
    viewModel: GastosViewModel,
    onClick: () -> Unit,
) {
    val pagadorNombre = gasto.pagadoPorNombre ?: "Desconocido"
    val colorAvatar = viewModel.getColorPorNombreDinamico(pagadorNombre)
    val iconoCategoria = getIconoCategoria(gasto.categoria)

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(
                imageVector = iconoCategoria,
                contentDescription = null,
                modifier = Modifier
                    .padding(top = 4.dp, end = 16.dp)
                    .size(24.dp),
                tint = Color.Black,
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(gasto.nombre, fontSize = 18.sp, fontWeight = FontWeight.W600)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AvatarConInicial(pagadorNombre, colorAvatar, size = 24.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Pagado por: $pagadorNombre",
                        fontSize = 13.sp,
                        color = ColorTextoGris,
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("${gasto.importe.toInt()}€", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier =
                        Modifier
                            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(50))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                ) {
                    Text("Compartido", fontSize = 10.sp)
                }
            }
        }
    }
}

@Composable
fun ItemGastoFijoEjemplo() {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = ColorLilaClaroTarjeta),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(Color.Black, RoundedCornerShape(4.dp))
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("Alquiler", fontSize = 18.sp, fontWeight = FontWeight.W600)
                    Text("Gasto Bimestral", fontSize = 12.sp, color = ColorTextoGris)
                }
            }
            Text("330€", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        Row(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row {
                AvatarConInicial("S", Color.Gray, 24.dp)
                Spacer(modifier = Modifier.width(4.dp))
                AvatarConInicial("J", Color.Black, 24.dp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Box(
                modifier =
                    Modifier
                        .weight(1f)
                        .border(1.dp, Color(0xFFD0B0F0), RoundedCornerShape(50))
                        .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text("Pago completado", fontSize = 12.sp, color = ColorMoradoOscuro)
            }
        }
    }
}

@Composable
fun VistaDetalleGasto(
    gasto: Gasto,
    viewModel: GastosViewModel,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val pagadorNombre = gasto.pagadoPorNombre ?: "Desconocido"
    val colorAvatarPagador = viewModel.getColorPorNombreDinamico(pagadorNombre)
    val participantes = viewModel.obtenerParticipantesGasto(gasto.importe)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorFondo),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.DeleteOutline, contentDescription = "Borrar")
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(gasto.nombre, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text(gasto.fecha.take(10), fontSize = 14.sp, color = ColorTextoGris)
        }

        Spacer(modifier = Modifier.height(30.dp))

        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Text(
                "PAGADO POR",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = ColorTextoGris
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AvatarConInicial(pagadorNombre, colorAvatarPagador, 36.dp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(pagadorNombre, fontSize = 16.sp)
                    }
                    Text("${gasto.importe}€", fontSize = 16.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            modifier =
                Modifier
                    .padding(horizontal = 20.dp)
                    .weight(1f),
        ) {
            Text(
                "PARTICIPANTES DEL PAGO",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = ColorTextoGris
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(participantes) { part ->
                    ItemParticipante(part)
                }
            }
        }

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
            contentAlignment = Alignment.Center,
        ) {
            OutlinedButton(
                onClick = onEdit,
                modifier = Modifier.width(200.dp),
                shape = RoundedCornerShape(50),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ColorTextoGris),
            ) {
                Text("Modificar")
            }
        }
    }
}

@Composable
fun ItemParticipante(part: ParticipantePago) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(0.dp),
    ) {
        Row(
            modifier =
                Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AvatarConInicial(part.nombre, part.colorAvatar, 36.dp)
                Spacer(modifier = Modifier.width(12.dp))
                Text(part.nombre, fontSize = 16.sp)
            }
            Text(
                "${String.format("%.2f", part.cantidad).replace('.', ',')}€",
                fontSize = 16.sp,
                color = ColorTextoGris
            )
        }
    }
}

@Composable
fun VistaEstadisticas(
    stats: List<PieChartData>,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
            }
            Spacer(modifier = Modifier.weight(1f))
            Text("Estadísticas", color = ColorTextoGris, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(20.dp))
        Text(
            "¿Cuánto\nhas pagado?",
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 40.sp,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(40.dp))

        Box(modifier = Modifier.size(320.dp)) {
            if (stats.isEmpty()) {
                Text("Sin datos", modifier = Modifier.align(Alignment.Center))
            } else {
                stats.take(5).forEachIndexed { index, data ->
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
                            )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            stats.take(4).forEach {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier
                        .size(12.dp)
                        .background(it.color, CircleShape))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(it.categoria, fontSize = 14.sp, color = ColorTextoGris)
                }
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun BubbleShape(
    text: String,
    color: Color,
    size: androidx.compose.ui.unit.Dp? = null,
    width: androidx.compose.ui.unit.Dp? = null,
    height: androidx.compose.ui.unit.Dp? = null,
    shape: androidx.compose.ui.graphics.Shape,
    rotation: Float,
    offset: DpOffset,
    modifier: Modifier,
) {
    Box(
        modifier =
            modifier
                .offset(x = offset.x, y = offset.y)
                .rotate(rotation)
                .then(
                    if (size != null) {
                        Modifier.size(size)
                    } else {
                        Modifier.size(width = width!!, height = height!!)
                    },
                )
                .clip(shape)
                .background(color),
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
fun NuevoGastoDialog(
    onDismiss: () -> Unit,
    gastoEditar: Gasto? = null,
    onConfirm: (String, String, String) -> Unit,
) {
    var nombre by remember { mutableStateOf(gastoEditar?.nombre ?: "") }
    var importe by remember { mutableStateOf(gastoEditar?.importe?.toString() ?: "") }
    var categoriaSelected by remember { mutableStateOf(gastoEditar?.categoria ?: "OTROS") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    if (gastoEditar ==
                        null
                    ) {
                        "Nuevo Gasto"
                    } else {
                        "Editar Gasto"
                    },
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorTextoGris,
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = ColorTextoGris)
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre del gasto") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                )

                OutlinedTextField(
                    value = importe,
                    onValueChange = { importe = it },
                    label = { Text("Importe (€)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                )

                Text("Categoría:", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CategoryChip("COMIDA", categoriaSelected) { categoriaSelected = it }
                    CategoryChip("OCIO", categoriaSelected) { categoriaSelected = it }
                    CategoryChip("OTROS", categoriaSelected) { categoriaSelected = it }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nombre.isNotEmpty() && importe.isNotEmpty()) {
                        onConfirm(nombre, importe, categoriaSelected)
                        onDismiss()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.Black),
            ) {
                Text("Aceptar", color = Color.Black)
            }
        },
    )
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
        colors =
            FilterChipDefaults.filterChipColors(
                selectedContainerColor = ColorLila,
                containerColor = ColorFondo,
            ),
        border =
            FilterChipDefaults.filterChipBorder(
                enabled = true,
                selected = isSelected,
                borderColor = if (isSelected) ColorMoradoOscuro else Color.Gray,
            ),
    )
}

@Composable
fun AvatarConInicial(
    nombre: String,
    colorFondo: Color,
    size: androidx.compose.ui.unit.Dp = 40.dp,
) {
    val inicial = nombre.firstOrNull()?.toString()?.uppercase() ?: "?"
    Box(
        modifier =
            Modifier
                .size(size)
                .clip(CircleShape)
                .background(colorFondo),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = inicial,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = if (size < 30.dp) 12.sp else 16.sp,
        )
    }
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
