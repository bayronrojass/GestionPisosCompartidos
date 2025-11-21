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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gestionpisoscompartidos.model.Gasto

// Colores del diseño
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
                // --- VISTA ESTADÍSTICAS ---
                VistaEstadisticas(
                    stats = stats,
                    onBack = { viewModel.toggleVista(false) },
                )
            } else {
                // --- VISTA LISTA DE GASTOS ---
                VistaListaGastos(
                    gastos = gastos,
                    onVerEstadisticas = { viewModel.toggleVista(true) },
                )
            }

            // Diálogo para añadir gasto (Placeholder visual)
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
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Cabecera
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Gastos", fontSize = 36.sp, fontWeight = FontWeight.Bold)
                Text(
                    "Estadísticas",
                    fontSize = 14.sp,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable { onVerEstadisticas() },
                )
            }
        }

        // Toggle Gastos/Saldos (Visual)
        item {
            Row(
                modifier =
                    Modifier
                        .width(180.dp)
                        .background(Color.White, RoundedCornerShape(20.dp))
                        .padding(4.dp),
            ) {
                Box(
                    modifier =
                        Modifier
                            .weight(1f)
                            .height(26.dp)
                            .background(ColorLila, RoundedCornerShape(13.dp)),
                    contentAlignment = Alignment.Center,
                ) { Text("Gastos", fontSize = 12.sp) }
                Box(
                    modifier = Modifier.weight(1f).height(26.dp),
                    contentAlignment = Alignment.Center,
                ) { Text("Saldos", fontSize = 12.sp) }
            }
        }

        // Lista dinámica desde BD
        if (gastos.isEmpty()) {
            item { Text("No hay gastos aún.", color = Color.Gray) }
        } else {
            // Agrupar por si quieres cabeceras (opcional), aquí lista simple
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
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(gasto.nombre, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("Pagado por: ${gasto.pagadoPor}", fontSize = 12.sp, color = Color.Gray)
            }
            Text("${gasto.importe}€", fontSize = 18.sp, fontWeight = FontWeight.Bold)
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
        // Cabecera Estadísticas
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
            }
            Spacer(modifier = Modifier.weight(1f))
            Text("Estadísticas", color = Color.Gray)
        }

        Spacer(modifier = Modifier.height(20.dp))
        Text("¿Cuánto\nhas pagado?", fontSize = 32.sp, fontWeight = FontWeight.Bold, lineHeight = 36.sp)

        Spacer(modifier = Modifier.height(40.dp))

        // Gráfico de "Flor" (Burbujas)
        Box(modifier = Modifier.size(300.dp)) {
            stats.forEachIndexed { index, data ->
                // Posicionamiento simple de burbujas según índice para simular la flor
                val align =
                    when (index) {
                        0 -> Alignment.TopStart
                        1 -> Alignment.BottomEnd
                        2 -> Alignment.BottomStart
                        else -> Alignment.TopEnd
                    }
                // Tamaño relativo al porcentaje (mínimo 80dp)
                val size = (data.porcentaje * 300).dp.coerceAtLeast(80.dp)

                Box(
                    modifier =
                        Modifier
                            .align(align)
                            .size(size)
                            .clip(CircleShape)
                            .background(data.color),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(data.textoPorcentaje, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }

            if (stats.isEmpty()) {
                Text("Sin datos", modifier = Modifier.align(Alignment.Center))
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Leyenda
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            stats.forEach {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp).background(it.color, CircleShape))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(it.categoria, fontSize = 12.sp)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NuevoGastoDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)) {
                Text("Aceptar")
            }
        },
        title = { Text("Nuevo Gasto") },
        text = { Text("Formulario de gasto aquí...") },
        containerColor = Color.White,
    )
}
