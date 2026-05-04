package es.mirumi.es.ui.encuestas

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.mirumi.es.model.responses.EncuestaResponse
import es.mirumi.es.model.responses.OpcionResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EncuestasScreen(viewModel: EncuestasViewModel) {
    val encuestas by viewModel.encuestas.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true },
                containerColor = Color(0xFF2196F3),
            ) {
                Icon(Icons.Default.Add, contentDescription = "Crear Encuesta", tint = Color.White)
            }
        },
    ) { paddingValues ->
        if (encuestas.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No hay encuestas todavía. ¡Crea una!", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                items(encuestas) { encuesta ->
                    EncuestaCard(encuesta = encuesta, onVotar = { opcionId ->
                        viewModel.votar(encuesta.id, opcionId)
                    })
                }
            }
        }

        if (showDialog) {
            CrearEncuestaDialog(
                onDismiss = { showDialog = false },
                onConfirm = { titulo, opciones, color ->
                    viewModel.crearEncuesta(titulo, opciones, color)
                    showDialog = false
                },
            )
        }
    }
}

@Composable
fun EncuestaCard(
    encuesta: EncuestaResponse,
    onVotar: (Long) -> Unit,
) {
    var opcionSeleccionada by remember { mutableStateOf<Long?>(null) }
    val totalVotosEncuesta = encuesta.opciones.sumOf { it.totalVotos }
    val colorBase = parseColor(encuesta.colorHex)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colorBase.copy(alpha = 0.1f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = encuesta.titulo, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = colorBase)
            Text(text = "Por: ${encuesta.creadorNombre}", fontSize = 12.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(16.dp))

            val mostrarResultados = encuesta.haVotado || encuesta.estado == "CERRADA"

            encuesta.opciones.forEach { opcion ->
                if (mostrarResultados) {
                    ResultadoOpcion(opcion, totalVotosEncuesta, colorBase)
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clickable { opcionSeleccionada = opcion.id }
                                .padding(vertical = 4.dp),
                    ) {
                        RadioButton(
                            selected = opcionSeleccionada == opcion.id,
                            onClick = { opcionSeleccionada = opcion.id },
                            colors = RadioButtonDefaults.colors(selectedColor = colorBase),
                        )
                        Text(text = opcion.texto, fontSize = 14.sp)
                    }
                }
            }

            if (!mostrarResultados) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { opcionSeleccionada?.let { onVotar(it) } },
                    enabled = opcionSeleccionada != null,
                    modifier = Modifier.align(Alignment.End),
                    colors = ButtonDefaults.buttonColors(containerColor = colorBase),
                ) {
                    Text("Votar", color = Color.White)
                }
            } else {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Total de votos: $totalVotosEncuesta",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.align(Alignment.End),
                )
            }
        }
    }
}

@Composable
fun ResultadoOpcion(
    opcion: OpcionResponse,
    totalVotos: Int,
    colorBase: Color,
) {
    val porcentaje = if (totalVotos > 0) opcion.totalVotos.toFloat() / totalVotos else 0f

    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(text = opcion.texto, fontSize = 14.sp)
            Text(text = "${(porcentaje * 100).toInt()}%", fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
        LinearProgressIndicator(
            progress = { porcentaje },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .padding(top = 4.dp)
                    .clip(RoundedCornerShape(5.dp)),
            color = colorBase,
            trackColor = colorBase.copy(alpha = 0.2f),
        )
    }
}

@Composable
fun CrearEncuestaDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, List<String>, String) -> Unit,
) {
    var titulo by remember { mutableStateOf("") }
    var opciones by remember { mutableStateOf(listOf("", "")) }

    val paletaColores = listOf("#2196F3", "#4CAF50", "#FF9800", "#F44336", "#9C27B0")
    var colorSeleccionado by remember { mutableStateOf(paletaColores[0]) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva Encuesta", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = titulo,
                    onValueChange = { titulo = it },
                    label = { Text("Pregunta") },
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(16.dp))
                Text("Opciones", fontWeight = FontWeight.Medium)

                opciones.forEachIndexed { index, texto ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = texto,
                            onValueChange = { nuevoTexto ->
                                val nuevaLista = opciones.toMutableList()
                                nuevaLista[index] = nuevoTexto
                                opciones = nuevaLista
                            },
                            label = { Text("Opción ${index + 1}") },
                            modifier = Modifier.weight(1f),
                        )

                        if (opciones.size > 2) {
                            IconButton(onClick = {
                                val nuevaLista = opciones.toMutableList()
                                nuevaLista.removeAt(index)
                                opciones = nuevaLista
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "Eliminar", tint = Color.Red)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                TextButton(onClick = { opciones = opciones + "" }) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Añadir opción")
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Color de la tarjeta", fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    paletaColores.forEach { hex ->
                        Box(
                            modifier =
                                Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(parseColor(hex))
                                    .clickable { colorSeleccionado = hex }
                                    .padding(2.dp)
                                    .background(
                                        if (colorSeleccionado == hex) Color.Transparent else parseColor(hex),
                                        CircleShape,
                                    ),
                        ) {
                            if (colorSeleccionado == hex) {
                                Box(modifier = Modifier.fillMaxSize().background(Color.White.copy(alpha = 0.4f), CircleShape))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val opcionesValidas = opciones.filter { it.isNotBlank() }
                    if (titulo.isNotBlank() && opcionesValidas.size >= 2) {
                        onConfirm(titulo, opcionesValidas, colorSeleccionado)
                    }
                },
            ) {
                Text("Publicar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
    )
}

// Función auxiliar para convertir el HEX a Color de Compose
fun parseColor(hex: String): Color =
    try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: Exception) {
        Color(0xFF2196F3) // Azul por defecto si falla
    }
