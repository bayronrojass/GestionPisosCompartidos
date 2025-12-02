package com.example.gestionpisoscompartidos.ui.tareas

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
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

    // Obtener el usuario actual desde el SessionManager
    val usuarioActual =
        remember {
            val userId = sessionManager.fetchCurrentUserId()
            val userName = sessionManager.fetchUserEmail()
            // Crear un usuario temporal con la información de sesión
            if (userId != null && userName != null) {
                Usuario(id = userId, nombre = userName, correo = "")
            } else {
                null
            }
        }

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

    // Obtener tareas del usuario actual (para MIS TAREAS)
    val tareasUsuarioActual =
        remember(tareasFiltradas, usuarioActual) {
            tareasFiltradas.filter { tarea ->
                // Incluir tareas asignadas al usuario actual
                tarea.asignadoA?.id == usuarioActual?.id
            }
        }

    // Obtener otras tareas (para OTRAS)
    val otrasTareas =
        remember(tareasFiltradas, usuarioActual) {
            tareasFiltradas.filter { tarea ->
                // Incluir tareas que NO están asignadas al usuario actual
                // Esto incluye: tareas sin asignar y tareas asignadas a otros usuarios
                tarea.asignadoA?.id != usuarioActual?.id
            }
        }

    // Cargar datos iniciales
    LaunchedEffect(Unit) {
        val token = sessionManager.fetchAuthToken()
        if (token != null) {
            viewModel.cargarMiembros(token)
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
            if (selectedTab == 0) {
                FloatingActionButton(
                    onClick = { showCreateDialog = true },
                    containerColor = Color.Black,
                    contentColor = Color.White,
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Añadir")
                }
            }
        },
    ) { paddingValues ->

        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color(0xfff8f8f8)),
        ) {
            // TITULO
            Text(
                text = "Tareas",
                color = Color.Black,
                style = MaterialTheme.typography.displaySmall,
                modifier =
                    Modifier
                        .align(Alignment.TopStart)
                        .offset(x = 20.dp, y = 15.dp),
            )

            // TABS (Pendientes / Completadas)
            Box(
                modifier =
                    Modifier
                        .align(Alignment.TopStart)
                        .offset(x = 65.dp, y = 75.dp)
                        .requiredWidth(width = 260.dp)
                        .requiredHeight(height = 24.dp),
            ) {
                // Fondo blanco
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(26.dp))
                            .background(Color.White)
                            .shadow(4.dp, RoundedCornerShape(26.dp)),
                )

                // Fondo morado (Selector)
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth(0.5f)
                            .fillMaxHeight()
                            .align(if (selectedTab == 0) Alignment.CenterStart else Alignment.CenterEnd)
                            .clip(RoundedCornerShape(26.dp))
                            .background(Color(0xffddc1fb)),
                )

                // Textos
                Text(
                    text = "Pendientes",
                    color = Color.Black,
                    style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium),
                    modifier =
                        Modifier
                            .align(Alignment.CenterStart)
                            .offset(x = 22.dp)
                            .clickable { selectedTab = 0 }
                            .padding(horizontal = 10.dp, vertical = 2.dp),
                )
                Text(
                    text = "Completadas",
                    color = Color.Black,
                    style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium),
                    modifier =
                        Modifier
                            .align(Alignment.CenterEnd)
                            .offset(x = (-22).dp)
                            .clickable { selectedTab = 1 }
                            .padding(horizontal = 10.dp, vertical = 2.dp),
                )
            }

            // CONTENIDO PRINCIPAL
            LazyColumn(
                modifier =
                    Modifier
                        .align(Alignment.TopStart)
                        .offset(y = 115.dp)
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 150.dp),
            ) {
                when (selectedTab) {
                    0 -> {
                        // PESTAÑA PENDIENTES
                        // Sección: ASIGNACIÓN MENSUAL
                        item {
                            Text(
                                text = "ASIGNACIÓN MENSUAL",
                                color = Color.Black,
                                style = TextStyle(fontSize = 16.sp),
                                modifier = Modifier.padding(bottom = 10.dp),
                            )
                            AsignacionMensualComponent()
                        }

                        // Sección: MIS TAREAS (solo las asignadas al usuario actual)
                        if (tareasUsuarioActual.isNotEmpty()) {
                            item {
                                Spacer(modifier = Modifier.height(20.dp))
                                Text(
                                    text = "MIS TAREAS",
                                    color = Color.Black,
                                    style = TextStyle(fontSize = 16.sp),
                                    modifier = Modifier.padding(bottom = 10.dp),
                                )
                            }
                            items(tareasUsuarioActual) { tarea ->
                                TaskCardPendiente(
                                    tarea = tarea,
                                    esMia = true,
                                    onComplete = { viewModel.toggleCompletado(tarea) },
                                    onEdit = { taskToEdit = tarea },
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                        }

                        // Sección: OTRAS TAREAS (sin asignar o asignadas a otros)
                        if (otrasTareas.isNotEmpty()) {
                            item {
                                Spacer(modifier = Modifier.height(20.dp))
                                Text(
                                    text = "OTRAS",
                                    color = Color.Black,
                                    style = TextStyle(fontSize = 16.sp),
                                    modifier = Modifier.padding(bottom = 10.dp),
                                )
                            }
                            items(otrasTareas) { tarea ->
                                if (tarea.asignadoA != null && tarea.asignadoA?.id != usuarioActual?.id) {
                                    // Tarea asignada a otro usuario
                                    TaskCardOtra(
                                        tarea = tarea,
                                        onEdit = { taskToEdit = tarea },
                                        modifier = Modifier.fillMaxWidth(),
                                    )
                                } else {
                                    // Tarea sin asignar
                                    TaskCardPendiente(
                                        tarea = tarea,
                                        esMia = false,
                                        onComplete = { viewModel.toggleCompletado(tarea) },
                                        onEdit = { taskToEdit = tarea },
                                        modifier = Modifier.fillMaxWidth(),
                                    )
                                }
                            }
                        }

                        if (tareasFiltradas.isEmpty()) {
                            item {
                                Text(
                                    text = "No hay tareas pendientes",
                                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                                    textAlign = TextAlign.Center,
                                    color = Color.Gray,
                                )
                            }
                        }
                    }

                    1 -> {
                        // PESTAÑA COMPLETADAS
                        // Sección: SEMANALES
                        val tareasSemanales = tareasFiltradas.filter { it.frecuencia == "Diaria" || it.frecuencia == "Semanal" }
                        if (tareasSemanales.isNotEmpty()) {
                            item {
                                Text(
                                    text = "SEMANALES",
                                    color = Color.Black,
                                    style = TextStyle(fontSize = 16.sp),
                                    modifier = Modifier.padding(bottom = 10.dp),
                                )
                            }
                            items(tareasSemanales) { tarea ->
                                TaskCardCompletada(
                                    tarea = tarea,
                                    onUncomplete = { viewModel.toggleCompletado(tarea) },
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                        }

                        // Sección: MENSUALES
                        val tareasMensuales = tareasFiltradas.filter { it.frecuencia == "Mensual" }
                        if (tareasMensuales.isNotEmpty()) {
                            item {
                                Spacer(modifier = Modifier.height(20.dp))
                                Text(
                                    text = "MENSUALES",
                                    color = Color.Black,
                                    style = TextStyle(fontSize = 16.sp),
                                    modifier = Modifier.padding(bottom = 10.dp),
                                )
                            }
                            items(tareasMensuales) { tarea ->
                                TaskCardCompletadaSimple(
                                    tarea = tarea,
                                    onUncomplete = { viewModel.toggleCompletado(tarea) },
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                        }

                        if (tareasFiltradas.isEmpty()) {
                            item {
                                Text(
                                    text = "No hay tareas completadas",
                                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                                    textAlign = TextAlign.Center,
                                    color = Color.Gray,
                                )
                            }
                        }

                        // Botón "ver más" como en el diseño
                        item {
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(
                                text = "ver más",
                                color = Color(0xff6c6c6c),
                                textDecoration = TextDecoration.Underline,
                                style = TextStyle(fontSize = 14.sp),
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .clickable { }
                                        .padding(vertical = 10.dp),
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            }
        }
    }

    // --- DIÁLOGOS ---
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
// COMPONENTES VISUALES MEJORADOS - SIN BORDES GRISES
// ----------------------------------------------------------------

@Composable
fun AsignacionMensualComponent() {
    // Estado para controlar qué opción está seleccionada
    var opcionSeleccionada by remember { mutableStateOf("Rotación") }

    Card(
        colors =
            CardDefaults.cardColors(
                containerColor = Color.White,
            ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 0.dp, // Sin sombra
            ),
        shape = RoundedCornerShape(15.dp),
        border = BorderStroke(1.dp, Color.LightGray), // Borde gris
        modifier =
            Modifier
                .fillMaxWidth()
                .requiredHeight(60.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(all = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            // Botón Rotación
            BotonAsignacion(
                icono = R.drawable.iconorotar,
                texto = "Rotación",
                seleccionado = opcionSeleccionada == "Rotación",
                onClick = { opcionSeleccionada = "Rotación" },
            )

            // Botón Aleatorio
            BotonAsignacion(
                icono = R.drawable.iconoaleatorio,
                texto = "Aleatorio",
                seleccionado = opcionSeleccionada == "Aleatorio",
                onClick = { opcionSeleccionada = "Aleatorio" },
            )

            // Botón Manual
            BotonAsignacion(
                icono = R.drawable.iconomanual,
                texto = "Manual",
                seleccionado = opcionSeleccionada == "Manual",
                onClick = { opcionSeleccionada = "Manual" },
            )
        }
    }
}

@Composable
fun BotonAsignacion(
    icono: Int,
    texto: String,
    seleccionado: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val backgroundColor = if (seleccionado) Color.Black else Color.White
    val contentColor = if (seleccionado) Color.White else Color.Black

    Box(
        contentAlignment = Alignment.Center,
        modifier =
            modifier
                .clip(RoundedCornerShape(7.5.dp))
                .background(backgroundColor)
                .clickable(onClick = onClick)
                .shadow(
                    elevation = if (seleccionado) 4.dp else 0.dp,
                    shape = RoundedCornerShape(7.5.dp),
                ).padding(horizontal = 10.dp, vertical = 7.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Image(
                painter = painterResource(id = icono),
                contentDescription = texto,
                colorFilter = ColorFilter.tint(contentColor),
                modifier = Modifier.size(20.dp),
            )
            Text(
                text = texto,
                color = contentColor,
                fontSize = 16.sp,
            )
        }
    }
}

@Composable
fun TaskCardPendiente(
    tarea: Tarea,
    esMia: Boolean,
    onComplete: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        colors =
            CardDefaults.cardColors(
                containerColor = Color.White,
            ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 4.dp,
            ),
        shape = RoundedCornerShape(15.dp),
        modifier =
            modifier
                .clickable(onClick = onEdit),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(all = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.Start),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.Top),
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = tarea.nombre,
                    color = Color.Black,
                    style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Medium),
                    modifier = Modifier.fillMaxWidth(),
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Prioridad
                    when (tarea.prioridad?.lowercase() ?: "media") {
                        "alta" -> BadgePrioridad("Alta", Color(0xffff6490), Color(0xff581327))
                        "media" -> BadgePrioridad("Media", Color(0xffddc1fb), Color(0xff5d427a))
                        "baja" -> BadgePrioridad("Baja", Color(0xFFC8E6C9), Color(0xFF2E7D32))
                    }

                    // Hora
                    BadgeHora("de 16 a 17h", Color(0xffff6490), Color(0xff5a1428))
                }
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                // Enlace "Solicitar cambio" solo para tareas propias
                if (esMia) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier =
                            Modifier
                                .clickable { /* TODO: Lógica cambio */ }
                                .padding(vertical = 4.dp),
                    ) {
                        Text(
                            text = "Solicitar cambio",
                            color = Color(0xff6c6c6c),
                            textDecoration = TextDecoration.Underline,
                            style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Image(
                            painter = painterResource(id = R.drawable.iconocambio),
                            contentDescription = "Cambio",
                            colorFilter = ColorFilter.tint(Color(0xff6c6c6c)),
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }

                // Botón Completar
                OutlinedButton(
                    onClick = onComplete,
                    colors =
                        ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xff6c6c6c),
                            containerColor = Color.Transparent,
                        ),
                    border = BorderStroke(1.dp, Color(0xff6c6c6c)), // Borde fino gris
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(40.dp),
                ) {
                    Text("Completar", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
fun TaskCardOtra(
    tarea: Tarea,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        colors =
            CardDefaults.cardColors(
                containerColor = Color.White,
            ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 4.dp,
            ),
        shape = RoundedCornerShape(15.dp),
        modifier =
            modifier
                .clickable(onClick = onEdit),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(all = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.Start),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.Top),
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = tarea.nombre,
                    color = Color.Black,
                    style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Medium),
                    modifier = Modifier.fillMaxWidth(),
                )

                Text(
                    text = "Asignado a ${tarea.asignadoA?.nombre ?: "Sin asignar"}",
                    color = Color(0xff6c6c6c),
                    style = TextStyle(fontSize = 16.sp),
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                // Fecha, Prioridad y Hora en fila
                Row(
                    horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.Start),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "14 Nov",
                        color = Color.Black,
                        style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium),
                        textAlign = TextAlign.Center,
                    )

                    // Prioridad
                    when (tarea.prioridad?.lowercase() ?: "media") {
                        "alta" -> BadgePrioridad("Alta", Color(0xffff6490), Color(0xff581327))
                        "media" -> BadgePrioridad("Media", Color(0xffddc1fb), Color(0xff5d427a))
                        "baja" -> BadgePrioridad("Baja", Color(0xFFC8E6C9), Color(0xFF2E7D32))
                    }

                    // Hora
                    BadgeHora("de 16 a 17h", Color(0xffddc1fb), Color(0xff5d427a))
                }

                // Enlace "Recordar"
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier =
                        Modifier
                            .clickable { /* TODO: Lógica recordatorio */ }
                            .padding(vertical = 4.dp),
                ) {
                    Text(
                        text = "Recordar",
                        color = Color(0xff6c6c6c),
                        textDecoration = TextDecoration.Underline,
                        style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Image(
                        painter = painterResource(id = R.drawable.iconocampana),
                        contentDescription = "Recordar",
                        colorFilter = ColorFilter.tint(Color(0xff6c6c6c)),
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
        }
    }
}

@Composable
fun TaskCardCompletada(
    tarea: Tarea,
    onUncomplete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        colors =
            CardDefaults.cardColors(
                containerColor = Color.White,
            ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 4.dp,
            ),
        shape = RoundedCornerShape(15.dp),
        modifier = modifier,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 17.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.Top),
            horizontalAlignment = Alignment.End,
        ) {
            Text(
                text = tarea.nombre,
                color = Color.Black,
                style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Medium),
                modifier = Modifier.fillMaxWidth(),
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(32.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.fillMaxWidth(),
            ) {
                // Información de quién lo hizo
                Row(
                    horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.Start),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier =
                        Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White)
                            .border(1.dp, Color(0xff939393), RoundedCornerShape(20.dp)) // Solo este mantiene el borde sutil
                            .padding(start = 2.dp, end = 14.dp, top = 2.dp, bottom = 2.dp),
                ) {
                    // Avatar del usuario
                    Image(
                        painter = painterResource(id = R.drawable.ic_user),
                        contentDescription = "Avatar",
                        contentScale = ContentScale.Crop,
                        modifier =
                            Modifier
                                .size(34.dp)
                                .clip(CircleShape),
                    )
                    Text(
                        text = "Hecho por ${tarea.asignadoA?.nombre ?: "Alguien"}",
                        color = Color(0xff6c6c6c),
                        style = TextStyle(fontSize = 16.sp),
                        textAlign = TextAlign.End,
                    )
                }

                // Fecha con icono
                Row(
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_check),
                        contentDescription = "Fecha",
                        colorFilter = ColorFilter.tint(Color.Black),
                        modifier = Modifier.size(24.dp),
                    )
                    Text(
                        text = "09 Nov",
                        color = Color.Black,
                        style = TextStyle(fontSize = 16.sp),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Composable
fun TaskCardCompletadaSimple(
    tarea: Tarea,
    onUncomplete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        colors =
            CardDefaults.cardColors(
                containerColor = Color.White,
            ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 4.dp,
            ),
        shape = RoundedCornerShape(15.dp),
        modifier = modifier,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 10.dp, top = 18.dp, bottom = 18.dp),
        ) {
            Text(
                text = tarea.nombre,
                color = Color.Black,
                style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Medium),
                modifier = Modifier.weight(1f),
            )

            // Fecha con icono
            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_check),
                    contentDescription = "Fecha",
                    colorFilter = ColorFilter.tint(Color.Black),
                    modifier = Modifier.size(24.dp),
                )
                Text(
                    text = "5 Oct",
                    color = Color.Black,
                    style = TextStyle(fontSize = 16.sp),
                    textAlign = TextAlign.End,
                )
            }
        }
    }
}

// Badge de Prioridad
@Composable
fun BadgePrioridad(
    text: String,
    backgroundColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier =
            modifier
                .clip(RoundedCornerShape(17.dp))
                .background(backgroundColor)
                .padding(horizontal = 10.dp, vertical = 2.dp),
    ) {
        Text(
            text = text,
            color = textColor,
            style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium),
            textAlign = TextAlign.Center,
        )
    }
}

// Badge de Hora
@Composable
fun BadgeHora(
    text: String,
    backgroundColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier =
            modifier
                .clip(RoundedCornerShape(25.dp))
                .background(backgroundColor)
                .padding(horizontal = 15.dp, vertical = 6.dp),
    ) {
        Text(
            text = text,
            color = textColor,
            style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium),
            textAlign = TextAlign.Center,
        )
    }
}

// ----------------------------------------------------------------
// DIÁLOGOS (SE MANTIENEN IGUAL)
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
