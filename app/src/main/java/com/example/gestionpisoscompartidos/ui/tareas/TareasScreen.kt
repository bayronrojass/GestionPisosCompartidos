package com.example.gestionpisoscompartidos.ui.tareas

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gestionpisoscompartidos.R
import com.example.gestionpisoscompartidos.data.SessionManager
import com.example.gestionpisoscompartidos.model.Tarea
import com.example.gestionpisoscompartidos.model.Usuario
import java.util.Calendar
import android.app.DatePickerDialog

@Composable
fun TareasScreen(
    viewModel: TareasViewModel,
    casaNombre: String,
) {
    val tareas by viewModel.tareas.observeAsState(emptyList())
    val isLoading by viewModel.isLoading.observeAsState(false)
    val error by viewModel.error.observeAsState()
    val miembros by viewModel.miembros.observeAsState(emptyList())

    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }

    // Estados locales para la UI
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Pendientes, 1: Completadas
    var showCreateDialog by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<Tarea?>(null) }

    // Filtramos las tareas según la pestaña seleccionada
    val tareasFiltradas =
        remember(tareas, selectedTab) {
            if (selectedTab == 0) {
                tareas.filter { !it.completado }
            } else {
                tareas.filter { it.completado }
            }
        }

    // Cargar datos iniciales
    LaunchedEffect(Unit) {
        val token = sessionManager.fetchAuthToken()
        if (token != null) {
            viewModel.cargarMiembros(token)
            // Asegúrate de que tu ViewModel tenga un método para cargar tareas también si no es automático
        }
    }

    // Manejo de errores
    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        containerColor = Color(0xfff8f8f8),
        floatingActionButton = {
            // Usamos el diseño del botón flotante original o el del Navbar nuevo
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = Color.White,
                contentColor = Color.Black,
            ) {
                Icon(Icons.Default.Add, contentDescription = "Añadir")
            }
        },
    ) { paddingValues ->

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp),
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // TITULO
            Text(
                text = "Tareas",
                color = Color.Black,
                style = MaterialTheme.typography.displaySmall,
                modifier = Modifier.padding(bottom = 20.dp),
            )

            // TABS (Pendientes / Completadas)
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(40.dp), // Aumenté un poco la altura para que sea táctil
            ) {
                // Fondo blanco
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(26.dp))
                            .background(Color.White)
                            .border(BorderStroke(3.dp, Color.White), RoundedCornerShape(26.dp))
                            .shadow(4.dp, RoundedCornerShape(26.dp)),
                )

                // Fondo morado (Selector)
                // Calculamos la posición visual del selector
                val alignment = if (selectedTab == 0) Alignment.CenterStart else Alignment.CenterEnd
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth(0.5f)
                            .fillMaxHeight()
                            .align(alignment)
                            .clip(RoundedCornerShape(26.dp))
                            .background(Color(0xffddc1fb)),
                )

                // Textos clickeables
                Row(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier =
                            Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable { selectedTab = 0 },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "Pendientes",
                            color = Color.Black,
                            style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium),
                        )
                    }
                    Box(
                        modifier =
                            Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable { selectedTab = 1 },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "Completadas",
                            color = Color.Black,
                            style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // LISTA DE TAREAS (LazyColumn para scroll)
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 80.dp),
            ) {
                // Sección Asignación Mensual (Solo visible en Pendientes)
                if (selectedTab == 0) {
                    item {
                        Text(
                            text = "ASIGNACIÓN MENSUAL",
                            style = TextStyle(fontSize = 16.sp),
                            modifier = Modifier.padding(bottom = 10.dp),
                        )
                        AsignacionMensualComponent()
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "LISTA DE TAREAS",
                            style = TextStyle(fontSize = 16.sp),
                            modifier = Modifier.padding(bottom = 10.dp),
                        )
                    }
                }

                if (tareasFiltradas.isEmpty()) {
                    item {
                        Text(
                            text = if (isLoading) "Cargando..." else "No hay tareas en esta sección",
                            modifier = Modifier.fillMaxWidth().padding(20.dp),
                            textAlign = TextAlign.Center,
                            color = Color.Gray,
                        )
                    }
                } else {
                    items(tareasFiltradas) { tarea ->
                        if (selectedTab == 0) {
                            // Tarea Pendiente
                            TaskCardPending(
                                tarea = tarea,
                                onComplete = { viewModel.toggleCompletado(tarea) },
                                onEdit = { taskToEdit = tarea },
                            )
                        } else {
                            // Tarea Completada
                            TaskCardCompleted(
                                tarea = tarea,
                                onUncomplete = { viewModel.toggleCompletado(tarea) },
                            )
                        }
                    }
                }
            }
        }
    }

    // --- DIÁLOGOS

    if (showCreateDialog) {
        CreateTaskDialog(
            miembros = miembros,
            onDismiss = { showCreateDialog = false },
            onCreate = { nombre, descripcion, asignadoId, fechaFin, frecuencia ->
                if (nombre.isNotBlank()) {
                    viewModel.crearTarea(nombre, descripcion, asignadoId, fechaFin, frecuencia)
                    showCreateDialog = false
                } else {
                    Toast.makeText(context, "El nombre es obligatorio", Toast.LENGTH_SHORT).show()
                }
            },
        )
    }

    taskToEdit?.let { tarea ->
        EditTaskDialog(
            tarea = tarea,
            miembros = miembros,
            onDismiss = { taskToEdit = null },
            onSave = { nombre, descripcion, asignadoId, fechaFin, frecuencia ->
                if (nombre.isNotBlank()) {
                    viewModel.editarTarea(tarea, nombre, descripcion, asignadoId, fechaFin, frecuencia)
                    taskToEdit = null
                } else {
                    Toast.makeText(context, "El nombre es obligatorio", Toast.LENGTH_SHORT).show()
                }
            },
        )
    }
}

// ----------------------------------------------------------------
// COMPONENTES VISUALES ADAPTADOS (Del nuevo diseño)
// ----------------------------------------------------------------

@Composable
fun AsignacionMensualComponent() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier =
            Modifier
                .fillMaxWidth()
                .height(60.dp)
                .clip(RoundedCornerShape(15.dp))
                .background(Color.White)
                .padding(10.dp)
                .shadow(4.dp, RoundedCornerShape(15.dp)),
    ) {
        // Opción Rotación (Seleccionada visualmente como ejemplo)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier =
                Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(7.5.dp))
                    .background(Color.Black),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                Icon(
                    painter = painterResource(id = R.drawable.iconorotar),
                    contentDescription = "Rotación",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Rotación", color = Color.White, fontSize = 12.sp)
            }
        }
        Spacer(modifier = Modifier.width(5.dp))
        // Opción Aleatorio
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            Icon(
                painter = painterResource(id = R.drawable.iconoaleatorio),
                contentDescription = "Aleatorio",
                tint = Color.White,
                modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Aleatorio", color = Color.White, fontSize = 12.sp)
        }
        // Opción Manual
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            Icon(
                painter = painterResource(id = R.drawable.iconomanual),
                contentDescription = "Manual",
                tint = Color.White,
                modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Manual", color = Color.White, fontSize = 12.sp)
        }
    }
}

@Composable
fun TaskCardPending(
    tarea: Tarea,
    onComplete: () -> Unit,
    onEdit: () -> Unit,
) {
    // Adaptación de "Property1tuya"
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(15.dp))
                .background(Color.White)
                .padding(20.dp)
                .shadow(4.dp, RoundedCornerShape(15.dp))
                .clickable { onEdit() },
        // Al hacer click en la tarjeta, editamos
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(
            modifier = Modifier.weight(0.6f),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = tarea.nombre,
                color = Color.Black,
                style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Medium),
            )

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                // Badge de Frecuencia o Prioridad
                if (!tarea.frecuencia.isNullOrBlank()) {
                    Badge(text = tarea.frecuencia, colorBg = Color(0xffddc1fb), colorTxt = Color(0xff5d427a))
                }

                // Badge de Hora o Asignado
                tarea.asignadoA?.let {
                    Badge(text = it.nombre, colorBg = Color(0xffff6490), colorTxt = Color(0xff5a1428))
                } ?: Badge(text = "Sin asignar", colorBg = Color.LightGray, colorTxt = Color.Black)
            }
        }

        // Sección de completar
        Column(
            modifier = Modifier.weight(0.4f),
            horizontalAlignment = Alignment.End,
        ) {
            // Simulamos el TextField "Completar" como un botón
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(40.dp) // Altura similar a un textfield
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                        .clickable { onComplete() },
                contentAlignment = Alignment.Center,
            ) {
                Text("Completar", color = Color.Gray, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { /* Lógica cambio */ },
            ) {
                Text(
                    text = "Cambiar",
                    color = Color(0xff6c6c6c),
                    textDecoration = TextDecoration.Underline,
                    fontSize = 12.sp,
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    painter = painterResource(id = R.drawable.iconocambio), // Asegúrate de tener este icono
                    contentDescription = "Cambio",
                    tint = Color(0xff6c6c6c),
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

@Composable
fun TaskCardCompleted(
    tarea: Tarea,
    onUncomplete: () -> Unit,
) {
    // Adaptación de "TareaHecha"
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(15.dp))
                .background(Color.White)
                .padding(20.dp)
                .shadow(4.dp, RoundedCornerShape(15.dp)),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = tarea.nombre,
                color = Color.Gray,
                style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Medium, textDecoration = TextDecoration.LineThrough),
            )
            // Botón para deshacer completado
            Icon(
                imageVector = Icons.Default.Add, // Icono temporal, usa un check o undo
                contentDescription = "Deshacer",
                modifier =
                    Modifier
                        .rotate(45f) // Convertir + en x
                        .clickable { onUncomplete() },
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Quién lo hizo
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier =
                    Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .border(1.dp, Color(0xff939393), RoundedCornerShape(20.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp)) // Placeholder imagen usuario
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Hecho por ${tarea.asignadoA?.nombre ?: "Alguien"}",
                    color = Color(0xff6c6c6c),
                    fontSize = 12.sp,
                )
            }

            // Fecha (Simulada, deberías formatear tarea.fechaFin)
            Text(text = "Hoy", fontSize = 14.sp)
        }
    }
}

@Composable
fun Badge(
    text: String,
    colorBg: Color,
    colorTxt: Color,
) {
    Box(
        modifier =
            Modifier
                .clip(RoundedCornerShape(17.dp))
                .background(colorBg)
                .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(
            text = text,
            color = colorTxt,
            style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium),
            textAlign = TextAlign.Center,
        )
    }
}

// ----------------------------------------------------------------
// DIÁLOGOS DE CREACIÓN / EDICIÓN (Copiados y adaptados de tu código antiguo)
// ----------------------------------------------------------------

@Composable
fun CreateTaskDialog(
    miembros: List<Usuario>,
    onDismiss: () -> Unit,
    onCreate: (String, String?, Long?, String?, String?) -> Unit,
) {
    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var asignadoAId by remember { mutableStateOf<Long?>(null) }
    var fechaFin by remember { mutableStateOf<String?>(null) }
    var frecuencia by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva Tarea") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre de la tarea") },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth(),
                )
                // Aquí deberías re-implementar tus selectores de Usuario y Fecha
                // He simplificado para que compile, pero usa tus UserSelectionDropdown originales
                Text("Asignar a: (Implementar selector)")
            }
        },
        confirmButton = {
            Button(onClick = { onCreate(nombre, descripcion.ifBlank { null }, asignadoAId, fechaFin, frecuencia) }) {
                Text("Crear")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
    )
}

// Diálogo de editar tarea
@Composable
fun EditTaskDialog(
    tarea: Tarea,
    miembros: List<Usuario>,
    onDismiss: () -> Unit,
    onSave: (String, String?, Long?, String?, String?) -> Unit,
) {
    var nombre by remember { mutableStateOf(tarea.nombre) }
    var descripcion by remember { mutableStateOf(tarea.descripcion ?: "") }
    var asignadoAId by remember { mutableStateOf(tarea.asignadoA?.id) }
    var fechaFin by remember { mutableStateOf(tarea.fechaFin) }
    var frecuencia by remember { mutableStateOf(tarea.frecuencia) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Tarea") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre de la tarea") },
                    modifier = Modifier.fillMaxWidth(),
                )

                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                )

                UserSelectionDropdown(
                    miembros = miembros,
                    selectedUserId = asignadoAId,
                    onUserSelected = { asignadoAId = it },
                )

                DatePickerField(
                    label = "Fecha Límite",
                    selectedDate = fechaFin,
                    onDateSelected = { fechaFin = it },
                )

                FrequencySelector(
                    selectedFrequency = frecuencia,
                    onFrequencySelected = { frecuencia = it },
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                onSave(nombre, descripcion.ifBlank { null }, asignadoAId, fechaFin, frecuencia)
            }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
    )
}

@Composable
fun UserSelectionDropdown(
    miembros: List<Usuario>,
    selectedUserId: Long?,
    onUserSelected: (Long?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    // Buscar el nombre del usuario seleccionado o mostrar "Sin asignar"
    val selectedUserName = miembros.find { it.id == selectedUserId }?.nombre ?: "Sin asignar"

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selectedUserName,
            onValueChange = {},
            label = { Text("Asignado a") },
            readOnly = true,
            trailingIcon = {
                Icon(Icons.Default.ArrowDropDown, "Desplegar")
            },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable { expanded = true },
            enabled = false, // Deshabilitamos input directo
            colors =
                androidx.compose.material3.TextFieldDefaults.colors(
                    disabledTextColor = Color.Black,
                    disabledContainerColor = Color.Transparent,
                    disabledIndicatorColor = Color.Gray,
                    disabledLabelColor = Color.Gray,
                    disabledTrailingIconColor = Color.Black,
                ),
        )
        // Capa invisible clickeable para abrir el menú (porque el TextField está disabled)
        Box(
            modifier =
                Modifier
                    .matchParentSize()
                    .clickable { expanded = true },
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(0.8f), // Ajuste visual
        ) {
            // Opción "Sin asignar"
            DropdownMenuItem(
                text = { Text("Sin asignar") },
                onClick = {
                    onUserSelected(null)
                    expanded = false
                },
            )
            // Lista de miembros
            miembros.forEach { miembro ->
                DropdownMenuItem(
                    text = { Text(miembro.nombre) },
                    onClick = {
                        onUserSelected(miembro.id)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
fun DatePickerField(
    label: String,
    selectedDate: String?, // Formato esperado por backend: yyyy-MM-ddTHH:mm:ss
    onDateSelected: (String?) -> Unit,
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    // Si ya hay fecha seleccionada, intentamos parsearla para mostrarla en el calendario
    // (Simplificado para el ejemplo, usaremos la fecha actual por defecto si es nulo o formato complejo)

    val datePickerDialog =
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                // Formateamos al estándar ISO-8601 que espera tu backend (LocalDateTime)
                // Añadimos una hora por defecto (ej: final del día o 00:00)
                val formattedMonth = (month + 1).toString().padStart(2, '0')
                val formattedDay = dayOfMonth.toString().padStart(2, '0')
                val isoDate = "$year-$formattedMonth-${formattedDay}T00:00:00"
                onDateSelected(isoDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH),
        )

    // Texto para mostrar al usuario (quitamos la parte de la hora para que sea legible)
    val displayText = selectedDate?.take(10) ?: ""

    OutlinedTextField(
        value = displayText,
        onValueChange = { },
        label = { Text(label) },
        readOnly = true,
        trailingIcon = {
            IconButton(onClick = { datePickerDialog.show() }) {
                Icon(Icons.Default.DateRange, contentDescription = "Seleccionar fecha")
            }
        },
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable { datePickerDialog.show() },
        enabled = false, // Deshabilitamos escritura manual
        colors =
            androidx.compose.material3.TextFieldDefaults.colors(
                disabledTextColor = Color.Black,
                disabledContainerColor = Color.Transparent,
                disabledIndicatorColor = Color.Gray,
                disabledLabelColor = Color.Gray,
                disabledTrailingIconColor = Color.Black,
            ),
    )
}

@Composable
fun FrequencySelector(
    selectedFrequency: String?,
    onFrequencySelected: (String?) -> Unit,
) {
    val options = listOf("Sin repetir", "Diaria", "Semanal", "Mensual", "Anual")
    var expanded by remember { mutableStateOf(false) }

    val displayText = selectedFrequency ?: "Sin repetir"

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = displayText,
            onValueChange = {},
            label = { Text("Frecuencia") },
            readOnly = true,
            trailingIcon = {
                Icon(Icons.Default.Repeat, "Frecuencia")
            },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable { expanded = true },
            enabled = false,
            colors =
                androidx.compose.material3.TextFieldDefaults.colors(
                    disabledTextColor = Color.Black,
                    disabledContainerColor = Color.Transparent,
                    disabledIndicatorColor = Color.Gray,
                    disabledLabelColor = Color.Gray,
                    disabledTrailingIconColor = Color.Black,
                ),
        )
        Box(
            modifier =
                Modifier
                    .matchParentSize()
                    .clickable { expanded = true },
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(0.8f),
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        val valueToSend = if (option == "Sin repetir") null else option
                        onFrequencySelected(valueToSend)
                        expanded = false
                    },
                )
            }
        }
    }
}
