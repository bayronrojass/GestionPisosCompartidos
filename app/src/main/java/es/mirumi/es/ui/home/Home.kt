package es.mirumi.es.ui.home

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.HowToVote
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import es.mirumi.es.R
import es.mirumi.es.data.SessionManager
import es.mirumi.es.model.Evento
import es.mirumi.es.model.Tarea
import es.mirumi.es.model.dtos.UsuarioRankingDTO
import es.mirumi.es.ui.navigation.Route
import es.mirumi.es.ui.pizarra.postits.DraggableViewModel
import es.mirumi.es.ui.pizarra.postits.DraggableViewModelFactory
import es.mirumi.es.ui.pizarra.postits.PizarraScreen
import es.mirumi.es.ui.utils.FabActionItem
import es.mirumi.es.ui.utils.FabActionType
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    casaId: Long,
    viewModel: HomeViewModel,
    onNavigateToMonthlyView: () -> Unit,
    navController: NavController,
) {
    val userName by viewModel.userName.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoadingUser.collectAsStateWithLifecycle()
    val houseName by viewModel.currentHouse.collectAsStateWithLifecycle()
    val selectedDate by viewModel.fechaSeleccionada.collectAsStateWithLifecycle()
    val next7days = viewModel.next7days()
    val topUser by viewModel.topUser.collectAsStateWithLifecycle()

    val eventos by viewModel.eventos.collectAsStateWithLifecycle()
    val tareas by viewModel.tareasDelUsuario.collectAsStateWithLifecycle()

    val scrollState = rememberScrollState()
    val isFloatingButtonVisible = remember { derivedStateOf { scrollState.value > 100 } }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        if (viewModel.currentUser.value == null && !isLoading) {
            viewModel.cargarUsuario()
        }
    }

    LaunchedEffect(Unit) {
        if (viewModel.currentHouse.value == null) {
            viewModel.cargarCasa()
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer =
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    viewModel.cargarRankingResumen()
                }
            }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold {
        Box(modifier = Modifier.fillMaxSize()) {
            HomeContent(
                modifier = modifier,
                viewModel = viewModel,
                userName = userName,
                houseName = houseName,
                selectedDate = selectedDate,
                next7days = next7days,
                scrollState = scrollState,
                onNavigateToMonthlyView = onNavigateToMonthlyView,
                eventos = eventos,
                tareas = tareas,
                casaId = casaId,
                navController = navController,
                topUser = topUser,
            )

            ScrollToTopFloatingButton(
                isVisible = isFloatingButtonVisible.value,
                onClick = { coroutineScope.launch { scrollState.animateScrollTo(0) } },
            )
        }
    }
}

@Composable
private fun HomeContent(
    modifier: Modifier,
    viewModel: HomeViewModel,
    userName: String,
    houseName: String?,
    selectedDate: LocalDate?,
    next7days: List<LocalDateTime>,
    scrollState: androidx.compose.foundation.ScrollState,
    onNavigateToMonthlyView: () -> Unit,
    eventos: List<Evento>,
    tareas: List<Tarea>,
    casaId: Long,
    navController: NavController,
    topUser: UsuarioRankingDTO?,
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(color = Color(0xfff8f8f8))
                .verticalScroll(scrollState),
    ) {
        HeaderSection(userName = userName, houseName = houseName)

        RankingBannerSection(
            topUser = topUser,
            onClick = { navController.navigate(Route.Ranking.createRoute(casaId)) },
        )

        EncuestasBannerSection(
            casaId = casaId,
            onClick = { navController.navigate(Route.Encuestas.createRoute(casaId)) },
        )

        Spacer(modifier = Modifier.height(16.dp))

        CalendarSection(
            list = next7days,
            selectedDate = selectedDate,
            onDateSelected = { date -> viewModel.seleccionarFecha(date) },
            viewModel = viewModel,
            onNavigateToMonthlyView = onNavigateToMonthlyView,
            eventos = eventos,
            tareas = tareas,
        )

        Spacer(modifier = Modifier.height(16.dp))

        WeeklyEvents(viewModel = viewModel, tareas = tareas, selectedDate = selectedDate ?: LocalDate.now())

        Spacer(modifier = Modifier.height(24.dp))

        NotificationsSection()

        Spacer(modifier = Modifier.height(24.dp))

        PendingTasksSection(viewModel)

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "ver más",
            color = Color(0xff6c6c6c),
            textDecoration = TextDecoration.Underline,
            style = TextStyle(fontSize = 14.sp),
            modifier = Modifier.align(alignment = Alignment.CenterHorizontally),
        )

        Spacer(modifier = Modifier.height(80.dp))
    }

    val pizarraFabActions = listOf(FabActionItem(icon = Icons.Default.NoteAdd, label = "Crear Post-it", action = FabActionType.POST_IT))
    val model =
        androidx.lifecycle.viewmodel.compose.viewModel<DraggableViewModel>(
            key = "Home",
            factory = DraggableViewModelFactory("Home", casaId, sessionManager),
        )

    PizarraScreen(
        model,
        fabActions = pizarraFabActions,
        onFabActionSelected = { action -> if (action.action == FabActionType.POST_IT) model.addNewPostIt() },
    )
}

@Composable
fun RankingBannerSection(
    topUser: UsuarioRankingDTO?,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(15.dp))
                .background(Color(0xFFFFD700).copy(alpha = 0.15f))
                .clickable { onClick() }
                .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(40.dp).background(Color(0xFFFFB300), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.EmojiEvents, contentDescription = "Trofeo", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text("Ranking de Convivencia", fontSize = 14.sp, color = Color.DarkGray, fontWeight = FontWeight.Bold)
                if (topUser != null) {
                    Text("🏆 ${topUser.nombre} lidera con ${topUser.puntos} pts", fontSize = 12.sp, color = Color(0xFFCD7F32))
                } else {
                    Text("🏆 Cargando líder...", fontSize = 12.sp, color = Color(0xFFCD7F32))
                }
            }
        }
        Icon(
            painter = painterResource(id = R.drawable.vector19),
            contentDescription = "Flecha",
            modifier = Modifier.size(16.dp).rotate(180f),
            tint = Color(0xFFFFB300),
        )
    }
}

@Composable
fun EncuestasBannerSection(
    casaId: Long,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(15.dp))
                .background(Color(0xFF2196F3).copy(alpha = 0.15f))
                .clickable { onClick() }
                .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(40.dp).background(Color(0xFF2196F3), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.HowToVote, contentDescription = "Encuestas", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text("Encuestas del Piso", fontSize = 14.sp, color = Color.DarkGray, fontWeight = FontWeight.Bold)
                Text("¡Vota en las decisiones conjuntas!", fontSize = 12.sp, color = Color(0xFF1976D2))
            }
        }
        Icon(
            painter = painterResource(id = R.drawable.vector19),
            contentDescription = "Flecha",
            modifier = Modifier.size(16.dp).rotate(180f),
            tint = Color(0xFF2196F3),
        )
    }
}

@Composable
private fun ScrollToTopFloatingButton(
    isVisible: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.BottomCenter) {
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
            exit =
                fadeOut() + slideOutVertically(targetOffsetY = { it }),
        ) {
            Icon(
                painter =
                    painterResource(
                        id = R.drawable.frame_125,
                    ),
                contentDescription = "Volver arriba",
                tint = Color.Unspecified,
                modifier =
                    Modifier
                        .size(80.dp)
                        .clickable {
                            onClick()
                        }.padding(12.dp),
            )
        }
    }
}

@Composable
fun HeaderSection(
    userName: String,
    houseName: String?,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text("Hola, $userName", color = Color.Black, style = TextStyle(fontSize = 36.sp, fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "$houseName",
                    color = Color.White,
                    style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium),
                    modifier =
                        Modifier
                            .clip(
                                MaterialTheme.shapes.medium,
                            ).background(Color(0xffff5686))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                )
                Icon(
                    painter = painterResource(id = R.drawable.home_ellipse41),
                    contentDescription = "Location",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
        Icon(
            painter = painterResource(id = R.drawable.dibujo_home),
            contentDescription = "Decoration",
            modifier = Modifier.size(120.dp),
            tint = Color.Unspecified,
        )
    }
}

@Composable
fun CalendarSection(
    list: List<LocalDateTime>,
    selectedDate: LocalDate? = null,
    onDateSelected: (LocalDate) -> Unit,
    viewModel: HomeViewModel,
    onNavigateToMonthlyView: () -> Unit,
    eventos: List<Evento>,
    tareas: List<Tarea>,
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = viewModel.mesesTraducidos(list.get(0).month.toString()),
                color = Color.Black,
                style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.SemiBold),
            )
            Text(
                text = "Vista mensual",
                color =
                    Color(
                        0xff6c6c6c,
                    ),
                textDecoration = TextDecoration.Underline,
                style = TextStyle(fontSize = 14.sp),
                modifier =
                    Modifier.clickable {
                        onNavigateToMonthlyView()
                    },
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            list.forEachIndexed { index, ldt ->
                val date = ldt.toLocalDate()
                val isSelected = selectedDate?.let { it == date } ?: false
                val hasEvent = eventos.any { viewModel.parseFechaSegura(it.fechaInicio) == date }
                val hasTask = tareas.any { !it.fechaFin.isNullOrEmpty() && viewModel.parseFechaSegura(it.fechaFin) == date }
                DayItem(
                    day = ldt.dayOfMonth.toString(),
                    dayName =
                        viewModel.diasTraducidos(
                            ldt.dayOfWeek,
                        ),
                    isToday = index == 0,
                    isSelected = isSelected,
                    hasEvent = hasEvent,
                    hasTask = hasTask,
                    onClick = {
                        onDateSelected(date)
                    },
                )
            }
        }
    }
}

@Composable
fun DayItem(
    day: String,
    dayName: String,
    isToday: Boolean = false,
    isSelected: Boolean = false,
    hasEvent: Boolean = false,
    hasTask: Boolean = false,
    onClick: () -> Unit,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Card(
            modifier = Modifier.size(50.dp),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = if (isSelected) Color(0xfffeee91) else Color.White),
            border = CardDefaults.outlinedCardBorder(),
            onClick = onClick,
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(text = day, style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium))
                Text(text = dayName, style = TextStyle(fontSize = 12.sp))
                if (hasEvent || hasTask) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        if (hasEvent) Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(Color(0xffff5686)))
                        if (hasTask) Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(Color(0xFFDDC1FB)))
                    }
                }
            }
        }
        if (isToday) Box(modifier = Modifier.size(8.dp).clip(MaterialTheme.shapes.small).background(Color(0xffff5686)))
    }
}

@Composable
fun WeeklyEvents(
    viewModel: HomeViewModel,
    tareas: List<Tarea>,
    selectedDate: LocalDate,
) {
    val eventosDelDia by viewModel.eventosDelDia.collectAsStateWithLifecycle()
    val tareasFiltradas =
        remember(tareas, selectedDate) {
            val limit = selectedDate.plusDays(6)
            tareas.filter {
                if (it.fechaFin.isNullOrEmpty()) return@filter false
                val fecha = viewModel.parseFechaSegura(it.fechaFin)
                !fecha.isBefore(selectedDate) && !fecha.isAfter(limit)
            }
        }
    val combinedList =
        remember(eventosDelDia, tareasFiltradas) {
            (eventosDelDia + tareasFiltradas).sortedBy {
                when (it) {
                    is Evento -> viewModel.parseFechaSegura(it.fechaInicio)
                    is Tarea -> viewModel.parseFechaSegura(it.fechaFin)
                    else -> LocalDate.MAX
                }
            }
        }
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
        if (combinedList.isNotEmpty()) {
            for (i in combinedList.indices) {
                val item = combinedList[i]
                val itemDate =
                    when (item) {
                        is Evento -> viewModel.parseFechaSegura(item.fechaInicio)
                        is Tarea -> viewModel.parseFechaSegura(item.fechaFin)
                        else -> LocalDate.now()
                    }
                val prevItemDate =
                    if (i > 0) {
                        when (val prev = combinedList[i - 1]) {
                            is Evento -> viewModel.parseFechaSegura(prev.fechaInicio)
                            is Tarea -> viewModel.parseFechaSegura(prev.fechaFin)
                            else -> LocalDate.MIN
                        }
                    } else {
                        null
                    }

                if (i == 0 || (prevItemDate != null && prevItemDate != itemDate)) {
                    Text(
                        text = viewModel.diasTraducidos(itemDate.dayOfWeek),
                        color = Color.Black,
                        style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Medium),
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
                val isSelectedDay = itemDate == selectedDate
                val cardColor = if (isSelectedDay) Color(0xfffff8cf) else Color.White
                if (item is Evento) {
                    EventItem(title = item.nombre, color = cardColor, creadoPor = item.creadoPor, viewModel = viewModel)
                } else if (item is Tarea) {
                    TimelineTaskItem(tarea = item, color = cardColor)
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun EventItem(
    title: String,
    color: Color,
    creadoPor: Long,
    viewModel: HomeViewModel,
) {
    var creadorName by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(creadoPor) {
        creadorName =
            try {
                viewModel.getUserNameById(creadoPor)
            } catch (e: Exception) {
                null
            }
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        colors = CardDefaults.cardColors(containerColor = color),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(8.dp).clip(MaterialTheme.shapes.small).background(Color(0xffff5686)))
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = title, style = TextStyle(fontSize = 15.sp), modifier = Modifier.weight(1f))
            Text(text = "Creado por: ${creadorName ?: "Cargando..."}", style = TextStyle(fontSize = 12.sp), modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun TimelineTaskItem(
    tarea: Tarea,
    color: Color,
) {
    val priorityColor =
        when (tarea.prioridad) {
            "Alta" -> Color(0xFFFF6490)
            "Media" -> Color(0xFFDDC1FB)
            else -> Color(0xFFA9E6A8)
        }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        colors = CardDefaults.cardColors(containerColor = color),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(8.dp).clip(MaterialTheme.shapes.small).background(Color(0xFFDDC1FB)))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = tarea.nombre, style = TextStyle(fontSize = 15.sp))
                Text(
                    text = "Asignado a: ${tarea.asignadoA?.nombre ?: "Sin asignar"}",
                    style = TextStyle(fontSize = 12.sp, color = Color.Gray),
                )
            }
            Text(
                text = tarea.prioridad ?: "",
                style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray),
                modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(priorityColor).padding(horizontal = 6.dp, vertical = 2.dp),
            )
        }
    }
}

@Composable
fun NotificationsSection() {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
        Text("NOTIFICACIONES", color = Color.Black, style = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.SemiBold))
        Spacer(modifier = Modifier.height(12.dp))
        NotificationItem("Marta te ha pagado 20€", true)
        Spacer(modifier = Modifier.height(8.dp))
        NotificationItem("Alguien te recuerda que limpies el baño", true)
    }
}

@Composable
fun NotificationItem(
    message: String,
    showMore: Boolean = false,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = message, style = TextStyle(fontSize = 16.sp), modifier = Modifier.weight(1f))
            if (showMore) {
                Text(
                    text = "Ver más",
                    color = Color(0xff6c6c6c),
                    textDecoration = TextDecoration.Underline,
                    style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium),
                )
            }
        }
    }
}

@Composable
fun PendingTasksSection(viewModel: HomeViewModel) {
    val tareasPendientes = viewModel.tareasDelUsuario.collectAsStateWithLifecycle()
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
        Text("MIS TAREAS PENDIENTES", color = Color.Black, style = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.SemiBold))
        Spacer(modifier = Modifier.height(12.dp))
        for (tarea in tareasPendientes.value) {
            val (color, textColor) =
                when (tarea.prioridad) {
                    "Alta" -> Pair(Color(0xFFFF6490), Color(0xFF581327))
                    "Media" -> Pair(Color(0xFFDDC1FB), Color(0xFF5D427A))
                    else -> Pair(Color(0xFFA9E6A8), Color(0xFF2D5C2C))
                }
            val date = viewModel.parseFechaSegura(tarea.fechaFin)
            val taskDate = "${viewModel.diasTraducidos(date.dayOfWeek)}. ${date.dayOfMonth}"
            PendingTaskItem(
                title = tarea.nombre,
                priority = tarea.prioridad!!,
                priorityColor = color,
                textColor = textColor,
                taskDate = taskDate,
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun PendingTaskItem(
    title: String,
    priority: String,
    priorityColor: Color,
    textColor: Color,
    taskDate: String?,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = title, style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Medium), modifier = Modifier.weight(1f))
                Text(
                    text = priority,
                    color = textColor,
                    style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium),
                    modifier =
                        Modifier
                            .clip(
                                MaterialTheme.shapes.large,
                            ).background(priorityColor)
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(text = taskDate ?: "nil", color = textColor, style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Solicitar cambio",
                        color = Color(0xff6c6c6c),
                        textDecoration = TextDecoration.Underline,
                        style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        painter = painterResource(id = R.drawable.home_iconocambio),
                        contentDescription = "Request change",
                        tint = Color(0xff6c6c6c),
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
    }
}
