package com.example.gestionpisoscompartidos.ui.gastos

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gestionpisoscompartidos.model.Gasto
import androidx.compose.material.icons.filled.EuroSymbol

// Colores UI generales
val ColorFondo = Color(0xFFF8F8F8)
val ColorLila = Color(0xFFDDC1FB)
val ColorTextoGris = Color(0xFF6C6C6C)
val ColorMoradoOscuro = Color(0xFF58337F)

@Composable
fun GastosScreen(viewModel: GastosViewModel) {
    val gastos by viewModel.gastos.collectAsState()
    val stats by viewModel.stats.collectAsState()
    val mostrarEstadisticas by viewModel.mostrarEstadisticas.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = ColorFondo,
        floatingActionButton = {
            if (!mostrarEstadisticas) {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
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
            } else {
                VistaListaGastos(
                    gastos = gastos,
                    onVerEstadisticas = { viewModel.toggleVista(true) },
                )
            }

            if (showAddDialog) {
                NuevoGastoDialog(onDismiss = { showAddDialog = false })
            }
        }
    }
}

@Composable
fun VistaListaGastos(
    gastos: List<Gasto>,
    onVerEstadisticas: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
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
                    modifier = Modifier.clickable { onVerEstadisticas() },
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier =
                    Modifier
                        .width(180.dp)
                        .background(Color.White, RoundedCornerShape(50.dp))
                        .padding(4.dp),
            ) {
                Box(
                    modifier =
                        Modifier
                            .weight(1f)
                            .height(32.dp)
                            .background(ColorLila, RoundedCornerShape(50.dp)),
                    contentAlignment = Alignment.Center,
                ) { Text("Gastos", fontSize = 14.sp, fontWeight = FontWeight.Medium) }
                Box(
                    modifier = Modifier.weight(1f).height(32.dp),
                    contentAlignment = Alignment.Center,
                ) { Text("Saldos", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = ColorTextoGris) }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        if (gastos.isEmpty()) {
            item { Text("No hay gastos aún.", color = Color.Gray) }
        } else {
            items(gastos) { gasto ->
                ItemGasto(gasto)
            }
        }
        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
fun ItemGasto(gasto: Gasto) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(
                imageVector = Icons.Default.EuroSymbol,
                contentDescription = null,
                modifier = Modifier.padding(top = 4.dp, end = 16.dp).size(24.dp),
                tint = Color.Black,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(gasto.nombre, fontSize = 18.sp, fontWeight = FontWeight.W600)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Pagado por: ${gasto.pagadoPorNombre ?: "Desconocido"}",
                    fontSize = 13.sp,
                    color = ColorTextoGris,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("${gasto.importe.toInt()}€", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
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

        // --- GRÁFICO DINÁMICO ---
        Box(modifier = Modifier.size(320.dp)) {
            if (stats.isEmpty()) {
                Text("Sin datos", modifier = Modifier.align(Alignment.Center))
            } else {
                // Dibujamos solo los primeros 4 o 5 para que no se sature
                stats.take(5).forEachIndexed { index, data ->
                    when (index) {
                        0 ->
                            BubbleShape( // 1º (Más gasto) -> Flor Grande
                                text = data.textoPorcentaje,
                                color = data.color,
                                size = 200.dp,
                                shape = RoundedCornerShape(40),
                                rotation = 10f,
                                offset = DpOffset(10.dp, 10.dp),
                                modifier = Modifier.align(Alignment.TopStart),
                            )
                        1 ->
                            BubbleShape( // 2º -> Círculo
                                text = data.textoPorcentaje,
                                color = data.color,
                                size = 150.dp,
                                shape = CircleShape,
                                rotation = 0f,
                                offset = DpOffset(20.dp, (-10).dp),
                                modifier = Modifier.align(Alignment.BottomStart),
                            )
                        2 ->
                            BubbleShape( // 3º -> Cuadrado Rotado
                                text = data.textoPorcentaje,
                                color = data.color,
                                size = 130.dp,
                                shape = RoundedCornerShape(30.dp),
                                rotation = -25f,
                                offset = DpOffset((-10).dp, 60.dp),
                                modifier = Modifier.align(Alignment.TopEnd),
                            )
                        3 ->
                            BubbleShape( // 4º -> Píldora
                                text = data.textoPorcentaje,
                                color = data.color,
                                width = 120.dp,
                                height = 70.dp,
                                shape = RoundedCornerShape(20.dp),
                                rotation = 5f,
                                offset = DpOffset(0.dp, 0.dp),
                                modifier = Modifier.align(Alignment.BottomEnd),
                            )
                        else -> { /* Ignorar o poner burbujitas pequeñas */ }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Leyenda
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
fun NuevoGastoDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = { Text("Nuevo Gasto") },
        text = { Text("Formulario aquí...") },
        confirmButton = {
            Button(onClick = onDismiss) { Text("Aceptar") }
        },
    )
}
