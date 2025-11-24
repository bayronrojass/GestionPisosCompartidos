package com.example.gestionpisoscompartidos.ui.gastos

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.SentimentSatisfiedAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gestionpisoscompartidos.model.Gasto

val ColorFondo = Color(0xFFF8F8F8)
val ColorLila = Color(0xFFDDC1FB)
val ColorLilaClaroTarjeta = Color(0xFFE8D5FC)
val ColorLilaSelected = Color(0xFFDDC1FB)
val ColorVerdeSaldo = Color(0xFF4CAF50)
val ColorRojoSaldo = Color(0xFFE57373)
val ColorTextoGris = Color(0xFF6C6C6C)
val ColorMoradoOscuro = Color(0xFF58337F)

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

@Composable
fun GastosScreen(viewModel: GastosViewModel) {
    val gastos by viewModel.gastos.collectAsState()
    val stats by viewModel.stats.collectAsState()
    val mostrarEstadisticas by viewModel.mostrarEstadisticas.collectAsState()

    var gastoSeleccionado by remember { mutableStateOf<Gasto?>(null) }
    var tabSeleccionado by remember { mutableIntStateOf(0) }

    var showDialog by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = ColorFondo,
        floatingActionButton = {
            if (!mostrarEstadisticas && gastoSeleccionado == null && tabSeleccionado == 0) {
                FloatingActionButton(
                    onClick = {
                        isEditing = false
                        showDialog = true
                    },
                    containerColor = Color.Black,
                    contentColor = Color.White,
                    shape = CircleShape,
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Añadir Gasto")
                }
            }
        },
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (mostrarEstadisticas) {
                VistaEstadisticas(
                    stats = stats,
                    onBack = { viewModel.toggleVista(false) },
                )
            } else if (gastoSeleccionado != null) {
                VistaDetalleGasto(
                    gasto = gastoSeleccionado!!,
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
                                            ).clickable { tabSeleccionado = 0 },
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
                                            ).clickable { tabSeleccionado = 1 },
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
                            onGastoClick = { gasto -> gastoSeleccionado = gasto },
                        )
                    } else {
                        VistaSaldosContent()
                    }
                }
            }

            if (showDialog) {
                NuevoGastoDialog(
                    onDismiss = { showDialog = false },
                    gastoEditar = if (isEditing) gastoSeleccionado else null,
                    onConfirm = { nombre, importe, categoria ->
                        if (isEditing && gastoSeleccionado != null) {
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
}

@Composable
fun VistaListaGastosContent(
    gastos: List<Gasto>,
    onGastoClick: (Gasto) -> Unit,
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        item {
            Text("GASTOS FIJOS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ColorTextoGris)
            Spacer(modifier = Modifier.height(8.dp))
        }
        item { ItemGastoFijoEjemplo() }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Hoy", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (gastos.isEmpty()) {
            item { Text("No hay gastos registrados.", color = Color.Gray) }
        } else {
            items(gastos) { gasto ->
                ItemGasto(gasto, onClick = { onGastoClick(gasto) })
            }
        }
        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
fun VistaSaldosContent() {
    val listaSaldos =
        listOf(
            SaldoUsuario("Natalia (yo)", 15.20, getColorPorNombre("Natalia")),
            SaldoUsuario("Raquel", -5.15, getColorPorNombre("Raquel")),
            SaldoUsuario("Daniel", 1.56, getColorPorNombre("Daniel")),
            SaldoUsuario("Marta", -3.50, getColorPorNombre("Marta")),
        )

    Column {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(1.dp),
        ) {
            Row(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
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
                    Text("¡Deudas zanjadas!", fontWeight = FontWeight.Bold)
                    Text("No necesitas compensar", fontSize = 12.sp, color = ColorTextoGris)
                }
                Icon(Icons.Default.KeyboardArrowRight, null)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("TRANSACCIONES", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ColorTextoGris)
        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(listaSaldos) { saldo ->
                ItemSaldo(saldo)
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
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(saldo.colorAvatar))
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
    onClick: () -> Unit,
) {
    val pagadorNombre = gasto.pagadoPorNombre ?: "Desconocido"
    val colorAvatar = getColorPorNombre(pagadorNombre)

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(
                imageVector = Icons.Default.RestaurantMenu,
                contentDescription = null,
                modifier = Modifier.padding(top = 4.dp, end = 16.dp).size(24.dp),
                tint = Color.Black,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(gasto.nombre, fontSize = 18.sp, fontWeight = FontWeight.W600)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(colorAvatar))
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
                    Text("Pagado por 4/4", fontSize = 10.sp)
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
            modifier = Modifier.padding(20.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(24.dp).background(Color.Black, RoundedCornerShape(4.dp)))
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("Alquiler", fontSize = 18.sp, fontWeight = FontWeight.W600)
                    Text("Gasto Bimestral", fontSize = 12.sp, color = ColorTextoGris)
                }
            }
            Text("330€", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row {
                AvatarSimulado(Color.Gray)
                Spacer(modifier = Modifier.width(4.dp))
                AvatarSimulado(Color.Black)
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
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val pagadorNombre = gasto.pagadoPorNombre ?: "Desconocido"
    val colorAvatarPagador = getColorPorNombre(pagadorNombre)

    val usuarios = listOf("Natalia (yo)", "Raquel", "Daniel", "Marta")
    val importePorPersona = gasto.importe / usuarios.size

    val participantes =
        usuarios.map { nombre ->
            ParticipantePago(
                nombre = nombre,
                cantidad = importePorPersona,
                colorAvatar = getColorPorNombre(nombre),
            )
        }

    Column(
        modifier = Modifier.fillMaxSize().background(ColorFondo),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
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
            Text("PAGADO POR", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ColorTextoGris)
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(colorAvatarPagador))
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
            Text("PARTICIPANTES DEL PAGO", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ColorTextoGris)
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
                Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(part.colorAvatar))
                Spacer(modifier = Modifier.width(12.dp))
                Text(part.nombre, fontSize = 16.sp)
            }
            Text("${String.format("%.2f", part.cantidad).replace('.', ',')}€", fontSize = 16.sp, color = ColorTextoGris)
        }
    }
}

@Composable
fun VistaEstadisticas(
    stats: List<PieChartData>,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
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
                    Box(modifier = Modifier.size(12.dp).background(it.color, CircleShape))
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
                ).clip(shape)
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
                modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp),
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
fun AvatarSimulado(
    color: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(color)
                .border(1.dp, Color.White, CircleShape),
    )
}

fun getColorPorNombre(nombre: String): Color =
    when {
        nombre.contains("Natalia", true) || nombre.contains("yo", true) -> Color(0xFFB1395B)
        nombre.contains("Daniel", true) -> Color(0xFF8061A2)
        nombre.contains("Marta", true) -> Color(0xFF93BBEC)
        nombre.contains("Raquel", true) -> Color(0xFF61995F)
        else -> Color.Gray
    }
