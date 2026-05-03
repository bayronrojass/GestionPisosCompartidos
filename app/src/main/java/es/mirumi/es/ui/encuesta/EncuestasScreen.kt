package es.mirumi.es.ui.encuestas

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import es.mirumi.es.model.responses.EncuestaResponse
import es.mirumi.es.model.responses.OpcionResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EncuestasScreen(viewModel: EncuestasViewModel) {
    val encuestas by viewModel.encuestas.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Crear Encuesta")
            }
        },
    ) { paddingValues ->
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

        if (showDialog) {
            CrearEncuestaDialog(
                onDismiss = { showDialog = false },
                onConfirm = { titulo, opciones ->
                    viewModel.crearEncuesta(titulo, opciones)
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

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = encuesta.titulo, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(text = "Creado por: ${encuesta.creadorNombre}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            Spacer(modifier = Modifier.height(12.dp))

            val mostrarResultados = encuesta.haVotado || encuesta.estado == "CERRADA"

            encuesta.opciones.forEach { opcion ->
                if (mostrarResultados) {
                    ResultadoOpcion(opcion, totalVotosEncuesta)
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = opcionSeleccionada == opcion.id,
                            onClick = { opcionSeleccionada = opcion.id },
                        )
                        Text(text = opcion.texto)
                    }
                }
            }

            if (!mostrarResultados) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { opcionSeleccionada?.let { onVotar(it) } },
                    enabled = opcionSeleccionada != null,
                    modifier = Modifier.align(Alignment.End),
                ) {
                    Text("Votar")
                }
            }
        }
    }
}

@Composable
fun ResultadoOpcion(
    opcion: OpcionResponse,
    totalVotos: Int,
) {
    val porcentaje = if (totalVotos > 0) opcion.totalVotos.toFloat() / totalVotos else 0f

    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(text = opcion.texto)
            Text(text = "${opcion.totalVotos} votos")
        }
        LinearProgressIndicator(
            progress = { porcentaje },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .padding(top = 4.dp),
        )
    }
}

@Composable
fun CrearEncuestaDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, List<String>) -> Unit,
) {
    var titulo by remember { mutableStateOf("") }
    var opcion1 by remember { mutableStateOf("") }
    var opcion2 by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva Encuesta") },
        text = {
            Column {
                OutlinedTextField(
                    value = titulo,
                    onValueChange = { titulo = it },
                    label = { Text("Pregunta (Ej: ¿Qué cenamos?)") },
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = opcion1,
                    onValueChange = { opcion1 = it },
                    label = { Text("Opción 1") },
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = opcion2,
                    onValueChange = { opcion2 = it },
                    label = { Text("Opción 2") },
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (titulo.isNotBlank() && opcion1.isNotBlank() && opcion2.isNotBlank()) {
                        onConfirm(titulo, listOf(opcion1, opcion2))
                    }
                },
            ) {
                Text("Crear")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
    )
}
