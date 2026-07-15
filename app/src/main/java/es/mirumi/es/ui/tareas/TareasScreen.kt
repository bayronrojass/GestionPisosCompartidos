package es.mirumi.es.ui.tareas

import TinderTaskCard
import android.app.DatePickerDialog
import android.content.Context
import android.content.ContextWrapper
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import es.mirumi.es.R
import es.mirumi.es.data.SessionManager
import es.mirumi.es.model.Tarea
import es.mirumi.es.model.Usuario
import es.mirumi.es.ui.gastos.AvatarConFoto
import es.mirumi.es.ui.gastos.ColorLila
import es.mirumi.es.ui.pizarra.postits.DraggableViewModel
import es.mirumi.es.ui.theme.Burgundy
import es.mirumi.es.ui.theme.ButtonShape
import es.mirumi.es.ui.theme.Fondo
import es.mirumi.es.ui.theme.LilaDark
import es.mirumi.es.ui.theme.LilaLight
import es.mirumi.es.ui.theme.LilaPrimary
import es.mirumi.es.ui.theme.TextoGris
import es.mirumi.es.ui.pizarra.postits.DraggableViewModelFactory
import es.mirumi.es.ui.pizarra.postits.PizarraScreen
import es.mirumi.es.ui.utils.FabActionItem
import es.mirumi.es.ui.utils.FabActionType
import es.mirumi.es.ui.utils.LogrosGlobalViewModel
import es.mirumi.es.ui.utils.LogrosGlobalViewModelFactory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

fun getIconoTarea(categoria: String?): ImageVector =
    when (categoria?.uppercase()) {
        "COCINA" -> Icons.Default.Restaurant
        "LIMPIEZA" -> Icons.Default.CleaningServices // O Icons.Default.Brush si da error
        "BAÑO" -> Icons.Default.Bathtub // O Icons.Default.WaterDrop si da error
        "COMPRAS", "COMPRA" -> Icons.Default.ShoppingCart
        else -> Icons.Default.Assignment
    }

fun Context.getActivity(): ComponentActivity? =
    when (this) {
        is ComponentActivity -> this
        is ContextWrapper -> baseContext.getActivity()
        else -> null
    }

@Composable
fun TareasScreen(
    viewModel: TareasViewModel,
    casaId: Long,
    userId: Long,
) {
    val tareas by viewModel.tareas.observeAsState(emptyList())
    val error by viewModel.error.observeAsState()
    val miembros by viewModel.miembros.observeAsState(emptyList())

    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }

    val activity = context.getActivity()
    val logrosGlobalViewModel: LogrosGlobalViewModel? =
        activity?.let {
            viewModel(
                viewModelStoreOwner = it,
                factory = remember { LogrosGlobalViewModelFactory(sessionManager) },
            )
        }

    val usuarioActual =
        remember {
            val uId = sessionManager.fetchCurrentUserId()
            val userName = sessionManager.fetchUserEmail()
            if (uId != null && userName != null) {
                Usuario(id = uId, nombre = userName, correo = userName)
            } else {
                null
            }
        }

    var selectedTab by remember { mutableIntStateOf(0) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<Tarea?>(null) }
    var showTinderDialog by remember { mutableStateOf(false) }

    val tareasFiltradas =
        remember(tareas, selectedTab) {
            if (selectedTab == 0) {
                tareas.filter { !it.completado }
            } else {
                tareas.filter { it.completado }
            }
        }

    val tareasUsuarioActual =
        remember(tareasFiltradas, usuarioActual) {
            tareasFiltradas.filter { tarea ->
                tarea.asignadoA?.id == usuarioActual?.id
            }
        }

    val otrasTareas =
        remember(tareasFiltradas, usuarioActual) {
            tareasFiltradas.filter { tarea ->
                tarea.asignadoA?.id != usuarioActual?.id
            }
        }

    fun daysDifference(dateString: String?): Long {
        if (dateString == null) return Long.MAX_VALUE
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val date = sdf.parse(dateString) ?: return Long.MAX_VALUE
            val now = Date()
            val diff = now.time - date.time
            TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS)
        } catch (e: Exception) {
            Long.MAX_VALUE
        }
    }

    val tareasPendientes = remember(tareas) { tareas.filter { !it.completado } }

    // 🔴 NUEVO: Filtramos SOLO las que no tienen dueño todavía para pasarlas al Tinder
    val tareasSinAsignar =
        remember(tareasPendientes) {
            tareasPendientes.filter { it.asignadoA == null }
        }

    val tareasCompletadas = remember(tareas) { tareas.filter { it.completado } }

    val tareasSemanales =
        remember(tareasCompletadas) {
            tareasCompletadas.filter { daysDifference(it.fechaFin) <= 7 }
        }

    val tareasMensuales =
        remember(tareasCompletadas) {
            tareasCompletadas.filter {
                val days = daysDifference(it.fechaFin)
                days > 7 && days <= 30
            }
        }

    LaunchedEffect(Unit) {
        val token = sessionManager.fetchAuthToken()
        if (token != null) {
            viewModel.cargarMiembros(token)
        }
    }

    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        containerColor = Fondo,
    ) { paddingValues ->

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Tareas",
                color = Color.Black,
                style = MaterialTheme.typography.displaySmall,
                modifier = Modifier.padding(horizontal = 20.dp),
            )

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier =
                    Modifier
                        .align(Alignment.CenterHorizontally)
                        .requiredWidth(260.dp)
                        .requiredHeight(24.dp),
            ) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(26.dp))
                            .background(Color.White)
                            .shadow(4.dp, RoundedCornerShape(26.dp)),
                )
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth(0.5f)
                            .fillMaxHeight()
                            .align(
                                if (selectedTab == 0) Alignment.CenterStart else Alignment.CenterEnd,
                            ).clip(RoundedCornerShape(26.dp))
                            .background(LilaLight),
                )
                Text(
                    text = "Pendientes",
                    color = Color.Black,
                    style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium),
                    modifier =
                        Modifier
                            .align(Alignment.CenterStart)
                            .clickable { selectedTab = 0 }
                            .padding(horizontal = 22.dp, vertical = 2.dp),
                )
                Text(
                    text = "Completadas",
                    color = Color.Black,
                    style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium),
                    modifier =
                        Modifier
                            .align(Alignment.CenterEnd)
                            .clickable { selectedTab = 1 }
                            .padding(horizontal = 22.dp, vertical = 2.dp),
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            LazyColumn(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 150.dp),
            ) {
                when (selectedTab) {
                    0 -> {
                        item {
                            Text(
                                "ASIGNACIÓN MENSUAL",
                                color = Color.Black,
                                style = TextStyle(fontSize = 16.sp),
                                modifier = Modifier.padding(bottom = 10.dp),
                            )
                            AsignacionMensualComponent()

                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { showTinderDialog = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = LilaLight),
                                shape = RoundedCornerShape(15.dp),
                            ) {
                                Icon(Icons.Default.ExpandMore, contentDescription = "Tinder", tint = LilaDark)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Reparto inteligente (Votar tareas)",
                                    color = LilaDark,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }

                        if (tareasUsuarioActual.isNotEmpty()) {
                            item {
                                Spacer(modifier = Modifier.height(20.dp))
                                Text(
                                    "MIS TAREAS",
                                    color = Color.Black,
                                    style = TextStyle(fontSize = 16.sp),
                                    modifier = Modifier.padding(bottom = 10.dp),
                                )
                            }
                            items(tareasUsuarioActual) { tarea ->
                                TaskCardPendiente(
                                    tarea = tarea,
                                    esMia = true,
                                    onComplete = {
                                        viewModel.toggleCompletado(tarea) {
                                            logrosGlobalViewModel?.comprobarNuevosLogros(silencioso = false)
                                        }
                                    },
                                    onEdit = { taskToEdit = tarea },
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                        }

                        if (otrasTareas.isNotEmpty()) {
                            item {
                                Spacer(modifier = Modifier.height(20.dp))
                                Text(
                                    "OTRAS",
                                    color = Color.Black,
                                    style = TextStyle(fontSize = 16.sp),
                                    modifier = Modifier.padding(bottom = 10.dp),
                                )
                            }
                            items(otrasTareas) { tarea ->
                                if (tarea.asignadoA != null && tarea.asignadoA?.id != usuarioActual?.id) {
                                    TaskCardOtra(
                                        tarea = tarea,
                                        onEdit = { taskToEdit = tarea },
                                        notificar = { viewModel.notificar(tarea) },
                                        modifier = Modifier.fillMaxWidth(),
                                    )
                                } else {
                                    TaskCardPendiente(
                                        tarea = tarea,
                                        esMia = false,
                                        onComplete = {
                                            viewModel.toggleCompletado(tarea) {
                                                logrosGlobalViewModel?.comprobarNuevosLogros(silencioso = false)
                                            }
                                        },
                                        onEdit = { taskToEdit = tarea },
                                        modifier = Modifier.fillMaxWidth(),
                                    )
                                }
                            }
                        }

                        if (tareasPendientes.isEmpty()) {
                            item {
                                Text(
                                    "No hay tareas pendientes",
                                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                                    textAlign = TextAlign.Center,
                                    color = Color.Gray,
                                )
                            }
                        }
                    }

                    1 -> {
                        if (tareasSemanales.isNotEmpty()) {
                            item {
                                Text(
                                    "SEMANALES",
                                    color = Color.Black,
                                    style = TextStyle(fontSize = 16.sp),
                                    modifier = Modifier.padding(bottom = 10.dp),
                                )
                            }
                            items(tareasSemanales) { tarea ->
                                TaskCardCompletada(
                                    tarea = tarea,
                                    viewModel = viewModel,
                                    onUncomplete = { viewModel.toggleCompletado(tarea) },
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                        }

                        if (tareasMensuales.isNotEmpty()) {
                            item {
                                Spacer(modifier = Modifier.height(20.dp))
                                Text(
                                    "MENSUALES",
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

                        if (tareasCompletadas.isEmpty()) {
                            item {
                                Text(
                                    "No hay tareas completadas",
                                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                                    textAlign = TextAlign.Center,
                                    color = Color.Gray,
                                )
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(
                                "ver más",
                                color =
                                    Color(
                                        0xff6c6c6c,
                                    ),
                                textDecoration = TextDecoration.Underline,
                                style =
                                    TextStyle(
                                        fontSize = 14.sp,
                                    ),
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                        }.padding(vertical = 10.dp),
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            }
        }
    }

    val pizarraFabActions =
        listOf(
            FabActionItem(icon = Icons.Default.NoteAdd, label = "Crear Post-it", action = FabActionType.POST_IT),
            FabActionItem(icon = Icons.Default.Add, label = "Crear Tarea", action = FabActionType.CREAR_TAREA),
        )

    val draggableViewModelKey = if (selectedTab == 0) "Tareas" else "TareasCompletadas"
    val model: DraggableViewModel =
        viewModel(
            key = draggableViewModelKey,
            factory =
                remember(draggableViewModelKey) {
                    DraggableViewModelFactory(draggableViewModelKey, casaId, sessionManager)
                },
        )

    PizarraScreen(
        model,
        fabActions = pizarraFabActions,
        onFabActionSelected = { action ->
            when (action.action) {
                FabActionType.POST_IT -> model.addNewPostIt()
                FabActionType.CREAR_TAREA -> showCreateDialog = true
                else -> {}
            }
        },
    )

    if (showCreateDialog) {
        NewCreateTaskDialog(
            miembros = miembros,
            onDismiss = { showCreateDialog = false },
            onCreate = { nombre, descripcion, asignadoId, fechaFin, frecuencia, prioridad ->
                if (nombre.isNotBlank()) {
                    viewModel.crearTarea(nombre, descripcion, asignadoId, fechaFin, frecuencia, prioridad, userId)
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
            onSave = { nombre, descripcion, asignadoId, fechaFin, frecuencia, prioridad ->
                if (nombre.isNotBlank()) {
                    viewModel.editarTarea(
                        tarea,
                        nombre,
                        descripcion,
                        asignadoId,
                        fechaFin,
                        frecuencia,
                        prioridad,
                        userId,
                    )
                    taskToEdit = null
                } else {
                    Toast.makeText(context, "El nombre es obligatorio", Toast.LENGTH_SHORT).show()
                }
            },
        )
    }

    if (showTinderDialog) {
        if (tareasSinAsignar.isEmpty()) {
            Toast.makeText(context, "No hay tareas sin asignar para repartir.", Toast.LENGTH_SHORT).show()
            showTinderDialog = false
        } else {
            RepartoTinderDialog(
                tareasPendientes = tareasSinAsignar,
                onDismiss = { showTinderDialog = false },
                onVote = { tareaId, puntuacion ->
                    viewModel.votarTarea(tareaId, puntuacion)
                },
                onFinish = {
                    viewModel.repartirTareas { mensajeServidor ->
                        Toast.makeText(context, mensajeServidor, Toast.LENGTH_LONG).show()
                    }
                    showTinderDialog = false
                },
            )
        }
    }
}

@Composable
fun AsignacionMensualComponent() {
    var opcionSeleccionada by remember { mutableStateOf("Rotación") }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(15.dp),
        border = BorderStroke(1.dp, Color.LightGray),
        modifier = Modifier.fillMaxWidth().requiredHeight(60.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxSize().padding(all = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            BotonAsignacion(
                icono = R.drawable.iconorotar,
                texto = "Rotación",
                seleccionado =
                    opcionSeleccionada == "Rotación",
                onClick = {
                    opcionSeleccionada =
                        "Rotación"
                },
            )
            BotonAsignacion(
                icono = R.drawable.iconoaleatorio,
                texto = "Aleatorio",
                seleccionado =
                    opcionSeleccionada == "Aleatorio",
                onClick = {
                    opcionSeleccionada =
                        "Aleatorio"
                },
            )
            BotonAsignacion(
                icono = R.drawable.iconomanual,
                texto = "Manual",
                seleccionado =
                    opcionSeleccionada == "Manual",
                onClick = {
                    opcionSeleccionada =
                        "Manual"
                },
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
                .clip(
                    RoundedCornerShape(7.5.dp),
                ).background(
                    backgroundColor,
                ).clickable(
                    onClick = onClick,
                ).shadow(
                    elevation = if (seleccionado) 4.dp else 0.dp,
                    shape = RoundedCornerShape(7.5.dp),
                ).padding(horizontal = 10.dp, vertical = 7.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            Image(
                painter = painterResource(id = icono),
                contentDescription = texto,
                colorFilter = ColorFilter.tint(contentColor),
                modifier = Modifier.size(20.dp),
            )
            Text(text = texto, color = contentColor, fontSize = 16.sp)
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
    val (colorFondoHora, colorTextoHora) =
        when (tarea.prioridad?.lowercase() ?: "media") {
            "alta" -> Pair(Color(0xffff6490), Burgundy)
            "media" -> Pair(LilaLight, LilaDark)
            "baja" -> Pair(Color(0xFFC8E6C9), Color(0xFF2E7D32))
            else -> Pair(LilaLight, LilaDark)
        }

    val fechaFinDisplay =
        remember(tarea.fechaFin) {
            tarea.fechaFin?.let {
                val partes = it.split("-", "T")
                if (partes.size >= 3) {
                    val dia = partes[2].take(2).toInt()
                    val mes =
                        when (partes[1]) {
                            "01" -> "Ene"
                            "02" -> "Feb"
                            "03" -> "Mar"
                            "04" -> "Abr"
                            "05" -> "May"
                            "06" -> "Jun"
                            "07" -> "Jul"
                            "08" -> "Ago"
                            "09" -> "Sep"
                            "10" -> "Oct"
                            "11" -> "Nov"
                            "12" -> "Dic"
                            else -> partes[1]
                        }
                    "$dia $mes"
                } else {
                    ""
                }
            } ?: ""
        }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(15.dp),
        modifier = modifier.clickable(onClick = onEdit),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(all = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.Start),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.Top), modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        imageVector = getIconoTarea(tarea.descripcion ?: "OTROS"),
                        contentDescription = "Icono Tarea",
                        tint = LilaDark,
                        modifier = Modifier.size(24.dp),
                    )
                    Text(
                        text = tarea.nombre,
                        color = Color.Black,
                        style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Medium),
                        modifier = Modifier.weight(1f),
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.Start),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    when (tarea.prioridad?.lowercase() ?: "media") {
                        "alta" -> BadgePrioridad("Alta", Color(0xffff6490), Burgundy)
                        "media" -> BadgePrioridad("Media", LilaLight, LilaDark)
                        "baja" -> BadgePrioridad("Baja", Color(0xFFC8E6C9), Color(0xFF2E7D32))
                    }
                    BadgeHora("de 16 a 17h", colorFondoHora, colorTextoHora)
                }

                if (fechaFinDisplay.isNotBlank()) {
                    Text(
                        text = fechaFinDisplay,
                        color = Color.Black,
                        style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium),
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (esMia) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { }.padding(vertical = 4.dp),
                    ) {
                        Text(
                            "Solicitar cambio",
                            color = TextoGris,
                            textDecoration = TextDecoration.Underline,
                            style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Image(
                            painter = painterResource(id = R.drawable.iconocambio),
                            contentDescription = "Cambio",
                            colorFilter = ColorFilter.tint(TextoGris),
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }

                OutlinedButton(
                    onClick = onComplete,
                    colors =
                        ButtonDefaults.outlinedButtonColors(
                            contentColor = LilaPrimary,
                            containerColor = Color.Transparent,
                        ),
                    border = BorderStroke(1.dp, LilaPrimary),
                    shape = ButtonShape,
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
    notificar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val (colorFondoHora, colorTextoHora) =
        when (tarea.prioridad?.lowercase() ?: "media") {
            "alta" -> Pair(Color(0xffff6490), Burgundy)
            "media" -> Pair(LilaLight, LilaDark)
            "baja" -> Pair(Color(0xFFC8E6C9), Color(0xFF2E7D32))
            else -> Pair(LilaLight, LilaDark)
        }

    val fechaFinDisplay =
        remember(tarea.fechaFin) {
            tarea.fechaFin?.let {
                val partes = it.split("-", "T")
                if (partes.size >= 3) {
                    val dia = partes[2].take(2).toInt()
                    val mes =
                        when (partes[1]) {
                            "01" -> "Ene"
                            "02" -> "Feb"
                            "03" -> "Mar"
                            "04" -> "Abr"
                            "05" -> "May"
                            "06" -> "Jun"
                            "07" -> "Jul"
                            "08" -> "Ago"
                            "09" -> "Sep"
                            "10" -> "Oct"
                            "11" -> "Nov"
                            "12" -> "Dic"
                            else -> partes[1]
                        }
                    "$dia $mes"
                } else {
                    ""
                }
            } ?: ""
        }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(15.dp),
        modifier = modifier.clickable(onClick = onEdit),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(all = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = tarea.nombre,
                        color = Color.Black,
                        style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Medium),
                    )
                    Text(
                        text = "Asignado a ${tarea.asignadoA?.nombre ?: "Sin asignar"}",
                        color = TextoGris,
                        style = TextStyle(fontSize = 16.sp),
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { notificar() }.padding(vertical = 4.dp),
                ) {
                    Text(
                        "Recordar",
                        color = TextoGris,
                        textDecoration = TextDecoration.Underline,
                        style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Image(
                        painter = painterResource(id = R.drawable.iconocampana),
                        contentDescription = "Recordar",
                        colorFilter = ColorFilter.tint(TextoGris),
                        modifier = Modifier.size(24.dp),
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (fechaFinDisplay.isNotBlank()) {
                    Text(
                        text = fechaFinDisplay,
                        color = Color.Black,
                        style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium),
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    when (tarea.prioridad?.lowercase() ?: "media") {
                        "alta" -> BadgePrioridad("Alta", Color(0xffff6490), Burgundy)
                        "media" -> BadgePrioridad("Media", LilaLight, LilaDark)
                        "baja" -> BadgePrioridad("Baja", Color(0xFFC8E6C9), Color(0xFF2E7D32))
                    }
                    BadgeHora("de 16 a 17h", colorFondoHora, colorTextoHora)
                }
            }
        }
    }
}

@Composable
fun TaskCardCompletada(
    tarea: Tarea,
    viewModel: TareasViewModel,
    onUncomplete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Same fix pattern as `GastosScreen.ItemGasto`: observe the members map at the composable
    // level so a proper Compose subscription is created. Without this, `tarea.asignadoA?.fotoUrl`
    // is read once at first render — before `cargarMiembros` has emitted — and never refreshes
    // until the user forces a recomposition (e.g., completing a new task). By deriving `fotoUrl`
    // from the observed `miembros` list (with the tarea's own `fotoUrl` as fallback for edge cases
    // where the assignee left the casa), the avatar pops in the microsecond the flow emits.
    val miembros by viewModel.miembros.observeAsState(emptyList())
    val fotoUrl =
        miembros.firstOrNull { it.id == tarea.asignadoA?.id }?.fotoUrl
            ?: tarea.asignadoA?.fotoUrl

    val fechaFinDisplay =
        remember(tarea.fechaFin) {
            tarea.fechaFin?.let {
                val partes = it.split("-", "T")
                if (partes.size >= 3) {
                    val dia = partes[2].take(2).toInt()
                    val mes =
                        when (partes[1]) {
                            "01" -> "Ene"
                            "02" -> "Feb"
                            "03" -> "Mar"
                            "04" -> "Abr"
                            "05" -> "May"
                            "06" -> "Jun"
                            "07" -> "Jul"
                            "08" -> "Ago"
                            "09" -> "Sep"
                            "10" -> "Oct"
                            "11" -> "Nov"
                            "12" -> "Dic"
                            else -> partes[1]
                        }
                    "$dia $mes"
                } else {
                    ""
                }
            } ?: ""
        }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(15.dp),
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 17.dp, bottom = 20.dp),
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
                Row(
                    horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.Start),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier =
                        Modifier
                            .clip(
                                RoundedCornerShape(20.dp),
                            ).background(
                                Color.White,
                            ).border(
                                1.dp,
                                Color(0xff939393),
                                RoundedCornerShape(20.dp),
                            ).padding(start = 2.dp, end = 14.dp, top = 2.dp, bottom = 2.dp),
                ) {
                    AvatarConFoto(
                        nombre = tarea.asignadoA?.nombre ?: "?",
                        colorFondo = ColorLila,
                        fotoUrl = fotoUrl,
                        size = 34.dp,
                    )
                    Text(
                        "Hecho por ${tarea.asignadoA?.nombre ?: "Alguien"}",
                        color = TextoGris,
                        style = TextStyle(fontSize = 16.sp),
                        textAlign = TextAlign.End,
                    )
                }

                Row(horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter =
                            painterResource(
                                id = R.drawable.ic_check,
                            ),
                        contentDescription = "Fecha",
                        colorFilter =
                            ColorFilter.tint(
                                Color.Black,
                            ),
                        modifier =
                            Modifier.size(24.dp).clickable {
                                onUncomplete()
                            },
                    )
                    Text(
                        text = if (fechaFinDisplay.isNotBlank()) fechaFinDisplay else "Sin fecha",
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
    val fechaFinDisplay =
        remember(tarea.fechaFin) {
            tarea.fechaFin?.let {
                val partes = it.split("-", "T")
                if (partes.size >= 3) {
                    val dia = partes[2].take(2).toInt()
                    val mes =
                        when (partes[1]) {
                            "01" -> "Ene"
                            "02" -> "Feb"
                            "03" -> "Mar"
                            "04" -> "Abr"
                            "05" -> "May"
                            "06" -> "Jun"
                            "07" -> "Jul"
                            "08" -> "Ago"
                            "09" -> "Sep"
                            "10" -> "Oct"
                            "11" -> "Nov"
                            "12" -> "Dic"
                            else -> partes[1]
                        }
                    "$dia $mes"
                } else {
                    ""
                }
            } ?: ""
        }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(15.dp),
        modifier = modifier,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 10.dp, top = 18.dp, bottom = 18.dp),
        ) {
            Text(
                text = tarea.nombre,
                color = Color.Black,
                style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Medium),
                modifier = Modifier.weight(1f),
            )

            Row(horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter =
                        painterResource(
                            id = R.drawable.ic_check,
                        ),
                    contentDescription = "Fecha",
                    colorFilter = ColorFilter.tint(Color.Black),
                    modifier =
                        Modifier.size(24.dp).clickable {
                            onUncomplete()
                        },
                )
                Text(
                    text = if (fechaFinDisplay.isNotBlank()) fechaFinDisplay else "Sin fecha",
                    color = Color.Black,
                    style = TextStyle(fontSize = 16.sp),
                    textAlign = TextAlign.End,
                )
            }
        }
    }
}

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
                .clip(
                    RoundedCornerShape(17.dp),
                ).background(backgroundColor)
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
                .clip(
                    RoundedCornerShape(25.dp),
                ).background(backgroundColor)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewCreateTaskDialog(
    miembros: List<Usuario>,
    onDismiss: () -> Unit,
    onCreate: (String, String?, Long?, String?, String?, String?) -> Unit,
) {
    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var asignadoAId by remember { mutableStateOf<Long?>(null) }
    var fechaFin by remember { mutableStateOf<String?>(null) }
    var prioridad by remember { mutableStateOf<String?>(null) }
    var frecuencia by remember { mutableStateOf<String?>(null) }
    var mostrarCampoNombre by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    val datePickerDialog =
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val formattedMonth = (month + 1).toString().padStart(2, '0')
                val formattedDay = dayOfMonth.toString().padStart(2, '0')
                val isoDate = "$year-$formattedMonth-${formattedDay}T00:00:00"
                fechaFin = isoDate
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH),
        )

    val fechaDisplay =
        remember(fechaFin) {
            fechaFin?.let {
                val partes = it.split("-")
                if (partes.size >= 3) {
                    val dia = partes[2].take(2).toInt()
                    val mes =
                        when (partes[1]) {
                            "01" -> "enero"
                            "02" -> "febrero"
                            "03" -> "marzo"
                            "04" -> "abril"
                            "05" -> "mayo"
                            "06" -> "junio"
                            "07" -> "julio"
                            "08" -> "agosto"
                            "09" -> "septiembre"
                            "10" -> "octubre"
                            "11" -> "noviembre"
                            "12" -> "diciembre"
                            else -> partes[1]
                        }
                    "$dia de $mes"
                } else {
                    ""
                }
            } ?: ""
        }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {},
        text = {
            Box(modifier = Modifier.requiredWidth(width = 250.dp).wrapContentHeight()) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(alignment = Alignment.TopEnd).offset(x = (-10).dp, y = (-10).dp),
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.iconocerrar),
                        contentDescription = "Cerrar",
                        colorFilter = ColorFilter.tint(TextoGris),
                        modifier = Modifier.size(24.dp),
                    )
                }

                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 15.dp),
                    verticalArrangement = Arrangement.spacedBy(11.dp),
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.Start),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            if (mostrarCampoNombre || nombre.isNotBlank()) {
                                OutlinedTextField(
                                    value = nombre,
                                    onValueChange = { nombre = it },
                                    label = { Text("Nombre de la tarea") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    colors =
                                        OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = LilaPrimary,
                                            unfocusedBorderColor = TextoGris,
                                        ),
                                )
                            } else {
                                Text(
                                    "Nombre tarea",
                                    color = TextoGris,
                                    style = MaterialTheme.typography.headlineSmall,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                        }
                        IconButton(onClick = { mostrarCampoNombre = true }, modifier = Modifier.size(24.dp)) {
                            Image(
                                painter = painterResource(id = R.drawable.iconolapiz),
                                contentDescription = "Editar",
                                colorFilter = ColorFilter.tint(TextoGris),
                            )
                        }
                    }

                    OutlinedTextField(
                        value = descripcion,
                        onValueChange = { descripcion = it },
                        label = { Text("Descripción (opcional)") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3,
                        colors =
                            OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = LilaPrimary,
                                unfocusedBorderColor = TextoGris,
                            ),
                    )

                    Text(
                        "Fecha tarea:",
                        color = Color.Black,
                        style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium),
                    )
                    InputChip(
                        label = {
                            Text(
                                if (fechaDisplay.isBlank()) "Seleccionar fecha" else fechaDisplay,
                                color = TextoGris,
                                style = TextStyle(fontSize = 14.sp),
                            )
                        },
                        avatar = {
                            Image(
                                painter = painterResource(id = R.drawable.iconocalendario),
                                contentDescription = "Calendario",
                                colorFilter = ColorFilter.tint(TextoGris),
                                modifier = Modifier.size(24.dp),
                            )
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = FilterChipDefaults.filterChipColors(containerColor = Color(0xfffbfafa)),
                        selected = fechaFin != null,
                        onClick = { datePickerDialog.show() },
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Text(
                        "Prioridad de la tarea:",
                        color = Color.Black,
                        style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium),
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        PrioridadChip(
                            texto = "Alta",
                            colorFondo = Color(0xffff6490),
                            colorTexto = Burgundy,
                            seleccionado =
                                prioridad == "alta",
                            onClick = { prioridad = "alta" },
                        )
                        PrioridadChip(
                            texto = "Media",
                            colorFondo = LilaLight,
                            colorTexto = LilaDark,
                            seleccionado =
                                prioridad == "media",
                            onClick = { prioridad = "media" },
                        )
                        PrioridadChip(
                            texto = "Baja",
                            colorFondo = Color(0xffa9e6a8),
                            colorTexto = Color(0xff2d5c2c),
                            seleccionado =
                                prioridad == "baja",
                            onClick = { prioridad = "baja" },
                        )
                    }

                    Text(
                        "Frecuencia:",
                        color = Color.Black,
                        style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium),
                    )
                    FrequencyChipSelector(
                        selectedFrequency = frecuencia,
                        onFrequencySelected = { frecuencia = it },
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Text(
                        "Asignar a:",
                        color = Color.Black,
                        style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium),
                    )
                    UserSelectionExpandable(miembros = miembros, selectedUserId = asignadoAId, onUserSelected = {
                        asignadoAId = it
                    }, modifier = Modifier.fillMaxWidth())

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            border = BorderStroke(1.dp, TextoGris),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.weight(1f),
                        ) {
                            Text("Cancelar", color = TextoGris)
                        }
                        Button(
                            onClick = {
                                if (nombre.isBlank()) {
                                    Toast.makeText(context, "El nombre es obligatorio", Toast.LENGTH_SHORT).show()
                                } else {
                                    onCreate(
                                        nombre,
                                        descripcion.ifBlank { null },
                                        asignadoAId,
                                        fechaFin,
                                        frecuencia,
                                        prioridad,
                                    )
                                }
                            },
                            shape = ButtonShape,
                            colors =
                                ButtonDefaults.buttonColors(
                                    containerColor = LilaPrimary,
                                    contentColor = Color.White,
                                ),
                            modifier = Modifier.weight(1f),
                        ) { Text("Aceptar") }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {},
        modifier = Modifier.padding(horizontal = 16.dp),
    )
}

@Composable
fun FrequencyChipSelector(
    selectedFrequency: String?,
    onFrequencySelected: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val options = listOf("Sin frecuencia", "Diaria", "Semanal", "Mensual", "Anual")
    var expanded by remember { mutableStateOf(false) }
    val displayText = selectedFrequency ?: "Sin frecuencia"

    Box(modifier = modifier) {
        OutlinedTextField(
            value = displayText,
            onValueChange = {},
            label = { Text("Seleccionar frecuencia") },
            readOnly = true,
            trailingIcon = { Icon(Icons.Default.ArrowDropDown, "Desplegar") },
            modifier = Modifier.fillMaxWidth().clickable { expanded = true },
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
        Box(modifier = Modifier.matchParentSize().clickable { expanded = true })

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(0.9f),
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        val valueToSend = if (option == "Sin frecuencia") null else option
                        onFrequencySelected(valueToSend)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
fun PrioridadChip(
    texto: String,
    colorFondo: Color,
    colorTexto: Color,
    seleccionado: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val backgroundColor = if (seleccionado) colorFondo else Color(0xfffbfafa)
    val textColor = if (seleccionado) colorTexto else TextoGris
    val borderColor = if (seleccionado) colorFondo else TextoGris

    Box(
        contentAlignment = Alignment.Center,
        modifier =
            modifier
                .clip(
                    RoundedCornerShape(17.dp),
                ).background(
                    backgroundColor,
                ).border(
                    width = 1.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(17.dp),
                ).clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 6.dp),
    ) {
        Text(
            text = texto,
            color = textColor,
            style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun UsuarioChip(
    nombre: String,
    seleccionado: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val backgroundColor = if (seleccionado) LilaLight else Color(0xfffbfafa)
    val borderColor = if (seleccionado) LilaLight else TextoGris

    Row(
        horizontalArrangement = Arrangement.spacedBy(7.dp, Alignment.Start),
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            modifier
                .clip(
                    shape = RoundedCornerShape(25.dp),
                ).background(
                    backgroundColor,
                ).border(
                    border = BorderStroke(1.dp, borderColor),
                    shape = RoundedCornerShape(25.dp),
                ).clickable(onClick = onClick)
                .padding(start = 8.dp, end = 12.dp, top = 4.dp, bottom = 4.dp),
    ) {
        Image(
            painter = painterResource(id = R.drawable.compartirnotaiconos),
            contentDescription = "Usuario",
            colorFilter = ColorFilter.tint(if (seleccionado) Color.White else TextoGris),
            modifier = Modifier.size(16.dp),
        )
        Text(
            text = nombre,
            color = if (seleccionado) Color.White else TextoGris,
            style = TextStyle(fontSize = 14.sp),
        )
    }
}

@Composable
fun UserSelectionChips(
    miembros: List<Usuario>,
    selectedUserId: Long?,
    onUserSelected: (Long?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        UsuarioChip(nombre = "Sin asignar", seleccionado = selectedUserId == null, onClick = {
            onUserSelected(null)
        }, modifier = Modifier.fillMaxWidth())
        miembros.forEach { miembro ->
            UsuarioChip(nombre = miembro.nombre, seleccionado = selectedUserId == miembro.id, onClick = {
                onUserSelected(miembro.id)
            }, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
fun EditTaskDialog(
    tarea: Tarea,
    miembros: List<Usuario>,
    onDismiss: () -> Unit,
    onSave: (String, String?, Long?, String?, String?, String?) -> Unit,
) {
    var nombre by remember { mutableStateOf(tarea.nombre) }
    var descripcion by remember { mutableStateOf(tarea.descripcion ?: "") }
    var asignadoAId by remember { mutableStateOf(tarea.asignadoA?.id) }
    var fechaFin by remember { mutableStateOf(tarea.fechaFin) }
    var frecuencia by remember { mutableStateOf(tarea.frecuencia) }
    var prioridad by remember { mutableStateOf(tarea.prioridad ?: "Media") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Tarea") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = nombre, onValueChange = {
                    nombre = it
                }, label = { Text("Nombre de la tarea") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = descripcion, onValueChange = {
                    descripcion = it
                }, label = { Text("Descripción (opcional)") }, modifier = Modifier.fillMaxWidth())
                UserSelectionDropdown(miembros = miembros, selectedUserId = asignadoAId, onUserSelected = {
                    asignadoAId =
                        it
                })
                DatePickerField(label = "Fecha Límite", selectedDate = fechaFin, onDateSelected = { fechaFin = it })
                FrequencySelector(selectedFrequency = frecuencia, onFrequencySelected = { frecuencia = it })
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        nombre,
                        descripcion.ifBlank { null },
                        asignadoAId,
                        fechaFin,
                        frecuencia,
                        prioridad,
                    )
                },
            ) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}

@Composable
fun UserSelectionDropdown(
    miembros: List<Usuario>,
    selectedUserId: Long?,
    onUserSelected: (Long?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedUserName = miembros.find { it.id == selectedUserId }?.nombre ?: "Sin asignar"

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selectedUserName,
            onValueChange = {},
            label = { Text("Asignado a") },
            readOnly = true,
            trailingIcon = { Icon(Icons.Default.ArrowDropDown, "Desplegar") },
            modifier = Modifier.fillMaxWidth().clickable { expanded = true },
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
        Box(modifier = Modifier.matchParentSize().clickable { expanded = true })

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(0.8f),
        ) {
            DropdownMenuItem(text = { Text("Sin asignar") }, onClick = {
                onUserSelected(null)
                expanded = false
            })
            miembros.forEach { miembro ->
                DropdownMenuItem(text = { Text(miembro.nombre) }, onClick = {
                    onUserSelected(miembro.id)
                    expanded =
                        false
                })
            }
        }
    }
}

@Composable
fun DatePickerField(
    label: String,
    selectedDate: String?,
    onDateSelected: (String?) -> Unit,
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val datePickerDialog =
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val formattedMonth = (month + 1).toString().padStart(2, '0')
                val formattedDay = dayOfMonth.toString().padStart(2, '0')
                onDateSelected("$year-$formattedMonth-${formattedDay}T00:00:00")
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH),
        )
    val displayText = selectedDate?.take(10) ?: ""

    OutlinedTextField(
        value = displayText,
        onValueChange = { },
        label = { Text(label) },
        readOnly = true,
        trailingIcon = {
            IconButton(onClick = {
                datePickerDialog.show()
            }) { Icon(Icons.Default.DateRange, contentDescription = "Seleccionar fecha") }
        },
        modifier = Modifier.fillMaxWidth().clickable { datePickerDialog.show() },
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
            trailingIcon = { Icon(Icons.Default.Repeat, "Frecuencia") },
            modifier = Modifier.fillMaxWidth().clickable { expanded = true },
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
        Box(modifier = Modifier.matchParentSize().clickable { expanded = true })

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(0.8f),
        ) {
            options.forEach { option ->
                DropdownMenuItem(text = { Text(option) }, onClick = {
                    onFrequencySelected(
                        if (option ==
                            "Sin repetir"
                        ) {
                            null
                        } else {
                            option
                        },
                    )
                    ; expanded = false
                })
            }
        }
    }
}

@Composable
fun UserSelectionExpandable(
    miembros: List<Usuario>,
    selectedUserId: Long?,
    onUserSelected: (Long?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, TextoGris),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column {
                Row(
                    modifier =
                        Modifier.fillMaxWidth().clickable { expanded = !expanded }.padding(
                            horizontal = 16.dp,
                            vertical = 12.dp,
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_user),
                            contentDescription = "Usuario",
                            modifier = Modifier.size(20.dp),
                        )
                        Text(
                            text = miembros.find { it.id == selectedUserId }?.nombre ?: "Sin asignar",
                            color = Color.Black,
                            style = TextStyle(fontSize = 14.sp),
                        )
                    }
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "Contraer" else "Expandir",
                        tint = TextoGris,
                    )
                }

                AnimatedVisibility(visible = expanded, enter = expandVertically(), exit = shrinkVertically()) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        OutlinedButton(
                            onClick = {
                                onUserSelected(null)
                                expanded = false
                            },
                            border =
                                BorderStroke(
                                    1.dp,
                                    if (selectedUserId ==
                                        null
                                    ) {
                                        LilaLight
                                    } else {
                                        TextoGris
                                    },
                                ),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors =
                                ButtonDefaults.outlinedButtonColors(
                                    containerColor =
                                        if (selectedUserId ==
                                            null
                                        ) {
                                            LilaLight
                                        } else {
                                            Color.Transparent
                                        },
                                ),
                        ) {
                            Text(
                                text = "Sin asignar",
                                color =
                                    if (selectedUserId ==
                                        null
                                    ) {
                                        Color.White
                                    } else {
                                        TextoGris
                                    },
                            )
                        }

                        HorizontalDivider(color = Color(0xffe0e0e0))

                        LazyColumn(
                            modifier = Modifier.heightIn(max = 150.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            items(miembros) { miembro ->
                                UserSelectionItem(usuario = miembro, selected = selectedUserId == miembro.id, onClick = {
                                    onUserSelected(miembro.id)
                                    expanded =
                                        false
                                })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UserSelectionItem(
    usuario: Usuario,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = if (selected) LilaLight else Color.White),
        border = BorderStroke(1.dp, if (selected) LilaLight else Color(0xffe0e0e0)),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_user),
                contentDescription = "Usuario",
                modifier = Modifier.size(32.dp),
            )
            Text(
                text = usuario.nombre,
                color = if (selected) Color.White else Color.Black,
                style = TextStyle(fontSize = 14.sp),
            )
        }
    }
}

@Composable
fun RepartoTinderDialog(
    tareasPendientes: List<Tarea>,
    onDismiss: () -> Unit,
    onVote: (tareaId: Long, puntuacion: Int) -> Unit,
    onFinish: () -> Unit,
) {
    var currentIndex by remember { mutableIntStateOf(0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "¡Reparto Justo!",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth().height(380.dp),
            ) {
                Text(
                    "Desliza a la DERECHA si te gustaría hacer la tarea, o a la IZQUIERDA si prefieres evitarla.",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp),
                )

                if (currentIndex < tareasPendientes.size) {
                    val tareaActual = tareasPendientes[currentIndex]

                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        key(tareaActual.id) {
                            TinderTaskCard(
                                tarea = tareaActual,
                                onVote = { meGusta ->
                                    val puntuacion = if (meGusta) 1 else -1
                                    onVote(tareaActual.id, puntuacion)
                                    currentIndex++
                                },
                            )
                        }
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(top = 40.dp),
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Listo",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(80.dp).padding(bottom = 16.dp),
                        )
                        Text("¡Has valorado todo!", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Pulsa en 'Repartir' para que el algoritmo asigne las tareas.",
                            textAlign = TextAlign.Center,
                            color = Color.Gray,
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (currentIndex >= tareasPendientes.size) {
                        onFinish()
                    }
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Burgundy),
            ) {
                Text(if (currentIndex < tareasPendientes.size) "Cerrar" else "¡Repartir Tareas!", color = Color.White)
            }
        },
        containerColor = Fondo,
    )
}

@Preview
@Composable
fun previewTareas() {
    TareasScreen(viewModel = viewModel(), 0, 0)
}

@Preview
@Composable
fun previewOtras() {
    TaskCardOtra(tarea = Tarea(1, "Tarea 1", null, false, null, null, false, null, null), {}, {})
}
