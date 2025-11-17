package com.example.gestionpisoscompartidos.ui.tareas

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gestionpisoscompartidos.model.Tarea
import com.google.android.material.progressindicator.CircularProgressIndicator
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material3.Divider
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun TareasScreen(
    viewModel: TareasViewModel,
    casaNombre: String,
) {
    val tareas by viewModel.tareas.observeAsState()
    val isLoading by viewModel.isLoading.observeAsState()
    val error by viewModel.error.observeAsState()
    val current = LocalContext.current

    // Manejo de errores
    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(current, it, Toast.LENGTH_LONG).show()
        }
    }

    // Estado para diálogos
    var showCreateDialog by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<Tarea?>(null) }
    var taskToView by remember { mutableStateOf<Tarea?>(null) }

    Tasks(
        modifier = Modifier.fillMaxSize(),
        casaNombre = casaNombre,
        isLoading = isLoading == true,
        tareas = tareas ?: listOf(),
        onAddTaskClick = { showCreateDialog = true },
        onTaskClick = { tarea ->
            taskToView = tarea
        },
        onTaskComplete = { tarea -> viewModel.toggleCompletado(tarea) },
        onTaskEdit = { tarea -> taskToEdit = tarea },
        onTaskDelete = { tarea ->
            // TODO: Implementar diálogo de confirmación de borrado
            viewModel.borrarTarea(tarea)
        },
        context = current,
    )

    // Diálogo de crear tarea
    if (showCreateDialog) {
        CreateTaskDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { nombre, descripcion ->
                if (nombre.isNotBlank()) {
                    viewModel.crearTarea(nombre, descripcion)
                    showCreateDialog = false
                } else {
                    Toast.makeText(current, "El nombre es obligatorio", Toast.LENGTH_SHORT).show()
                }
            },
        )
    }

    // Diálogo de editar tarea
    taskToEdit?.let { tarea ->
        EditTaskDialog(
            tarea = tarea,
            onDismiss = { taskToEdit = null },
            onSave = { nombre, descripcion ->
                if (nombre.isNotBlank()) {
                    viewModel.editarTarea(tarea, nombre, descripcion, null)
                    taskToEdit = null
                } else {
                    Toast.makeText(current, "El nombre es obligatorio", Toast.LENGTH_SHORT).show()
                }
            },
        )
    }

    // Diálogo detalles tarea
    taskToView?.let { tarea ->
        TaskDetailDialog(
            tarea = tarea,
            onDismiss = { taskToView = null },
        )
    }
}

// Diálogo de crear tarea
@Composable
fun CreateTaskDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String?) -> Unit,
) {
    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva Tarea") },
        text = {
            Column {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre de la tarea") },
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            Button(onClick = { onCreate(nombre, descripcion.ifBlank { null }) }) {
                Text("Crear")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
    )
}

// Diálogo de editar tarea
@Composable
fun EditTaskDialog(
    tarea: Tarea,
    onDismiss: () -> Unit,
    onSave: (String, String?) -> Unit,
) {
    var nombre by remember { mutableStateOf(tarea.nombre) }
    var descripcion by remember { mutableStateOf(tarea.descripcion ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Tarea") },
        text = {
            Column {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre de la tarea") },
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSave(nombre, descripcion.ifBlank { null }) }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
    )
}

// TasksScreen actualizado para recibir datos reales
@Composable
fun Tasks(
    modifier: Modifier = Modifier,
    casaNombre: String,
    isLoading: Boolean,
    tareas: List<Tarea>,
    onAddTaskClick: () -> Unit,
    onTaskClick: (Tarea) -> Unit,
    onTaskComplete: (Tarea) -> Unit,
    onTaskEdit: (Tarea) -> Unit,
    onTaskDelete: (Tarea) -> Unit,
    context: Context,
) {
    if (isLoading) {
        CircularProgressIndicator(context)
        return
    }

    val scrollState = rememberScrollState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onAddTaskClick) {
                Icon(Icons.Default.Add, "Añadir tarea")
            }
        },
    ) { padding ->
        Column(
            modifier =
                modifier
                    .padding(padding)
                    .background(color = Color(0xfff8f8f8))
                    .verticalScroll(scrollState)
                    .padding(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Header con nombre de la casa
            Text(
                text = "Tareas - $casaNombre",
                color = Color.Black,
                style = MaterialTheme.typography.displaySmall,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp),
            )

            // TODO: Implementar TaskStatusSelector y TaskAssignmentSection si son necesarios

            // Lista de tareas
            if (tareas.isEmpty()) {
                Text(
                    text = "No hay tareas",
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                    textAlign = TextAlign.Center,
                )
            } else {
                TaskListSection(
                    tareas = tareas,
                    onTaskClick = onTaskClick,
                    onTaskComplete = onTaskComplete,
                    onTaskEdit = onTaskEdit,
                    onTaskDelete = onTaskDelete,
                )
            }
        }
    }
}

// Componente para lista de tareas
@Composable
fun TaskListSection(
    tareas: List<Tarea>,
    onTaskClick: (Tarea) -> Unit,
    onTaskComplete: (Tarea) -> Unit,
    onTaskEdit: (Tarea) -> Unit,
    onTaskDelete: (Tarea) -> Unit,
) {
    Column(
        modifier = Modifier.padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "TODAS LAS TAREAS",
            color = Color.Black,
            style = TextStyle(fontSize = 17.sp),
        )

        tareas.forEach { tarea ->
            TaskCard(
                tarea = tarea,
                onTaskClick = { onTaskClick(tarea) },
                onTaskComplete = { onTaskComplete(tarea) },
                onTaskEdit = { onTaskEdit(tarea) },
                onTaskDelete = { onTaskDelete(tarea) },
            )
        }
    }
}

// Tarjeta de tarea con todas las acciones
@Composable
fun TaskCard(
    tarea: Tarea,
    onTaskClick: () -> Unit,
    onTaskComplete: () -> Unit,
    onTaskEdit: () -> Unit,
    onTaskDelete: () -> Unit,
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onTaskClick),
        shape = RoundedCornerShape(15.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Checkbox para completar
            Checkbox(
                checked = tarea.completado,
                onCheckedChange = { onTaskComplete() },
            )

            // Información de la tarea
            Column(
                modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = tarea.nombre,
                    color = Color.Black,
                    style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Medium),
                )
                tarea.descripcion?.let { descripcion ->
                    Text(
                        text = descripcion,
                        color = Color(0xff6c6c6c),
                        style = TextStyle(fontSize = 14.sp),
                    )
                }
            }

            // Menú de opciones
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, "Opciones")
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                ) {
                    DropdownMenuItem(
                        text = { Text("Editar") },
                        onClick = {
                            onTaskEdit()
                            showMenu = false
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("Eliminar") },
                        onClick = {
                            onTaskDelete()
                            showMenu = false
                        },
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun TasksScreenPreview() {
    Tasks(
        casaNombre = "Mi Casa",
        isLoading = false,
        tareas =
            listOf(
                Tarea(
                    id = 1,
                    nombre = "Tirar basura",
                    descripcion = "Sacar la basura",
                    completado = false,
                    fechaFin = "",
                    frecuencia = "",
                    periodica = false,
                    asignadoA = null,
                ),
                Tarea(
                    id = 2,
                    nombre = "Limpiar cocina",
                    descripcion = "Limpiar encimera",
                    completado = true,
                    fechaFin = "",
                    frecuencia = "",
                    periodica = false,
                    asignadoA = null,
                ),
            ),
        onAddTaskClick = {},
        onTaskClick = {},
        onTaskComplete = {},
        onTaskEdit = {},
        onTaskDelete = {},
        context = LocalContext.current,
    )
}

@Composable
fun TaskDetailDialog(
    tarea: Tarea,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = tarea.nombre,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Descripción
                if (!tarea.descripcion.isNullOrBlank()) {
                    Text(
                        text = tarea.descripcion,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Black,
                    )
                } else {
                    Text(
                        text = "Sin descripción adicional.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    )
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                // Estado
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (tarea.completado) Icons.Default.CheckCircle else Icons.Default.Pending,
                        contentDescription = null,
                        tint = if (tarea.completado) Color(0xFF4CAF50) else Color(0xFFFF9800),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (tarea.completado) "Completada" else "Pendiente",
                        fontWeight = FontWeight.Bold,
                        color = if (tarea.completado) Color(0xFF4CAF50) else Color(0xFFFF9800),
                    )
                }

                // Asignado a
                DetailRow(
                    icon = Icons.Default.Person,
                    label = "Asignado a:",
                    value = tarea.asignadoA?.nombre ?: "Sin asignar", // Asumiendo que Usuario tiene propiedad 'nombre'
                )

                // Fecha límite
                DetailRow(
                    icon = Icons.Default.CalendarToday,
                    label = "Fecha límite:",
                    value = tarea.fechaFin ?: "Sin fecha límite",
                )

                // Frecuencia (si es periódica)
                if (tarea.periodica) {
                    DetailRow(
                        icon = Icons.Default.Repeat,
                        label = "Frecuencia:",
                        value = tarea.frecuencia ?: "No especificada",
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        },
    )
}

// Componente auxiliar para filas de detalles
@Composable
fun DetailRow(
    icon: ImageVector,
    label: String,
    value: String,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier.padding(end = 8.dp),
        )
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black,
            )
        }
    }
}
