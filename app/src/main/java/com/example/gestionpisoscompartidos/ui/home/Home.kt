package com.example.gestionpisoscompartidos.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.gestionpisoscompartidos.R
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel,
    onNavigateToMonthlyView: () -> Unit,
) {
    val userName by viewModel.userName.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoadingUser.collectAsStateWithLifecycle()
    val houseName by viewModel.currentHouse.collectAsStateWithLifecycle()
    val selectedDate by viewModel.fechaSeleccionada.collectAsStateWithLifecycle()
    val next7days = viewModel.next7days()

    val scrollState = rememberScrollState()
    val isFloatingButtonVisible =
        remember {
            derivedStateOf { scrollState.value > 100 }
        }
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

    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        HomeContent(
            modifier = modifier,
            viewModel = viewModel,
            userName = userName,
            houseName = houseName,
            selectedDate = selectedDate,
            next7days = next7days,
            scrollState = scrollState,
            onNavigateToMonthlyView = onNavigateToMonthlyView,
        )

        ScrollToTopFloatingButton(
            isVisible = isFloatingButtonVisible.value,
            onClick = {
                coroutineScope.launch {
                    scrollState.animateScrollTo(0)
                }
            },
        )
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
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(color = Color(0xfff8f8f8))
                .verticalScroll(scrollState),
    ) {
        HeaderSection(
            userName = userName,
            houseName = houseName,
        )

        Spacer(modifier = Modifier.height(16.dp))

        CalendarSection(
            list = next7days,
            selectedDate = selectedDate,
            onDateSelected = { date -> viewModel.seleccionarFecha(date) },
            viewModel = viewModel,
            onNavigateToMonthlyView = onNavigateToMonthlyView,
        )

        Spacer(modifier = Modifier.height(16.dp))

        WeeklyEvents(viewModel)

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
}

@Composable
private fun ScrollToTopFloatingButton(
    isVisible: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .padding(16.dp),
        contentAlignment = Alignment.BottomCenter,
    ) {
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
        ) {
            Icon(
                painter = painterResource(id = R.drawable.frame_125),
                contentDescription = "Volver arriba",
                tint = Color.Unspecified,
                modifier =
                    Modifier
                        .size(80.dp)
                        .clickable { onClick() }
                        .padding(12.dp),
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
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(
                text = "Hola, $userName",
                color = Color.Black,
                style =
                    TextStyle(
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                    ),
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "$houseName",
                    color = Color.White,
                    style =
                        TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                        ),
                    modifier =
                        Modifier
                            .clip(MaterialTheme.shapes.medium)
                            .background(Color(0xffff5686))
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
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
    ) {
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
                color = Color(0xff6c6c6c),
                textDecoration = TextDecoration.Underline,
                style = TextStyle(fontSize = 14.sp),
                modifier = Modifier.clickable { onNavigateToMonthlyView() },
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            list.forEachIndexed { index, ldt ->
                val isSelected = selectedDate?.let { it == ldt.toLocalDate() } ?: false
                DayItem(
                    day = ldt.dayOfMonth.toString(),
                    dayName = viewModel.diasTraducidos(ldt.dayOfWeek),
                    isToday = index == 0,
                    isSelected = isSelected,
                    onClick = { onDateSelected(ldt.toLocalDate()) },
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
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Card(
            modifier = Modifier.size(50.dp),
            shape = MaterialTheme.shapes.medium,
            colors =
                CardDefaults.cardColors(
                    containerColor = if (isSelected) Color(0xfffeee91) else Color.White,
                ),
            border = CardDefaults.outlinedCardBorder(),
            onClick = onClick,
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = day,
                    style =
                        TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                        ),
                )
                Text(
                    text = dayName,
                    style = TextStyle(fontSize = 12.sp),
                )
            }
        }

        if (isToday) {
            Box(
                modifier =
                    Modifier
                        .size(8.dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(Color(0xffff5686)),
            )
        }
    }
}

@Composable
fun WeeklyEvents(viewModel: HomeViewModel) {
    val eventosDelDia by viewModel.eventosDelDia.collectAsStateWithLifecycle()
    val sortedList = viewModel.sortEvents(eventosDelDia)
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
    ) {
        if (sortedList.isNotEmpty()) {
            for (i in sortedList.indices) {
                val prev = if (i == 0) null else sortedList[i - 1]
                val day = sortedList[i]
                if (i == 0 || !viewModel.areSameDay(prev!!.fechaInicio, day.fechaInicio)) {
                    Text(
                        text = viewModel.diasTraducidos(viewModel.parseFechaSegura(day.fechaInicio).dayOfWeek),
                        color = Color.Black,
                        style =
                            TextStyle(
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Medium,
                            ),
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                EventItem(
                    title = day.nombre,
                    color = Color(0xfffff8cf),
                    creadoPor = day.creadoPor,
                    viewModel = viewModel,
                )

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
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier =
                    Modifier
                        .size(8.dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(Color(0xffff5686)),
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = title,
                style = TextStyle(fontSize = 15.sp),
                modifier = Modifier.weight(1f),
            )

            Text(
                text = "Creado por: ${creadorName ?: "Cargando..."}",
                style = TextStyle(fontSize = 12.sp),
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
fun NotificationsSection() {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
    ) {
        Text(
            text = "NOTIFICACIONES",
            color = Color.Black,
            style = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.SemiBold),
        )

        Spacer(modifier = Modifier.height(12.dp))

        NotificationItem(
            message = "Marta te ha pagado 20€",
            showMore = true,
        )

        Spacer(modifier = Modifier.height(8.dp))

        NotificationItem(
            message = "Alguien te recuerda que limpies el baño",
            showMore = true,
        )
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
            Text(
                text = message,
                style = TextStyle(fontSize = 16.sp),
                modifier = Modifier.weight(1f),
            )

            if (showMore) {
                Text(
                    text = "Ver más",
                    color = Color(0xff6c6c6c),
                    textDecoration = TextDecoration.Underline,
                    style =
                        TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                        ),
                )
            }
        }
    }
}

@Composable
fun PendingTasksSection(viewModel: HomeViewModel) {
    val tareasPendientes = viewModel.tareasDelUsuario.collectAsStateWithLifecycle()
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
    ) {
        Text(
            text = "MIS TAREAS PENDIENTES",
            color = Color.Black,
            style = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.SemiBold),
        )

        Spacer(modifier = Modifier.height(12.dp))

        for (tarea in tareasPendientes.value) {
            val (color, textColor) =
                when (tarea.prioridad) {
                    "Alta" -> Pair(Color(0xFFFF6490), Color(0xFF581327))
                    "Media" -> Pair(Color(0xFFDDC1FB), Color(0xFF5D427A))
                    else -> Pair(Color(0xFFA9E6A8), Color(0xFF2D5C2C))
                }

            val date = viewModel.parseFechaSegura(tarea.fechaFin!!)
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
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = title,
                    style =
                        TextStyle(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                        ),
                    modifier = Modifier.weight(1f),
                )

                Text(
                    text = priority,
                    color = textColor,
                    style =
                        TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                        ),
                    modifier =
                        Modifier
                            .clip(MaterialTheme.shapes.large)
                            .background(priorityColor)
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = taskDate ?: "nil",
                    color = textColor,
                    style =
                        TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                        ),
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Solicitar cambio",
                        color = Color(0xff6c6c6c),
                        textDecoration = TextDecoration.Underline,
                        style =
                            TextStyle(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                            ),
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
