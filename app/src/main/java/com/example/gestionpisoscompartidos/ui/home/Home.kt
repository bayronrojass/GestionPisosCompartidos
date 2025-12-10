package com.example.gestionpisoscompartidos.ui.home

import android.annotation.SuppressLint
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gestionpisoscompartidos.R
import com.example.gestionpisoscompartidos.model.Evento
import com.example.gestionpisoscompartidos.model.Tarea
import com.example.gestionpisoscompartidos.ui.pizarra.postits.DraggableViewModel
import com.example.gestionpisoscompartidos.ui.pizarra.postits.DraggableViewModelFactory
import com.example.gestionpisoscompartidos.ui.pizarra.postits.PizarraScreen
import com.example.gestionpisoscompartidos.ui.utils.FabActionItem
import com.example.gestionpisoscompartidos.ui.utils.FabActionType
import java.time.LocalDate
import java.time.LocalDateTime

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    casaId: Long,
    viewModel: HomeViewModel,
    onVistaMensualClick: () -> Unit, // Nuevo parámetro para navegación
) {
    val userName by viewModel.userName.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoadingUser.collectAsStateWithLifecycle()
    val houseName by viewModel.currentHouse.collectAsStateWithLifecycle()
    val selectedDate by viewModel.fechaSeleccionada.collectAsStateWithLifecycle()

    // Obtenemos eventos y tareas para los indicadores
    val eventos by viewModel.eventos.collectAsStateWithLifecycle()
    val tareas by viewModel.tareasDelUsuario.collectAsStateWithLifecycle()

    val next7days = viewModel.next7days()

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

    Scaffold {
        Column(
            modifier =
                modifier
                    .fillMaxSize()
                    .background(color = Color(0xfff8f8f8))
                    .verticalScroll(rememberScrollState()),
        ) {
            HeaderSection(userName = userName, houseName)

            Spacer(modifier = Modifier.height(16.dp))

            CalendarSection(
                list = next7days,
                selectedDate = selectedDate,
                onDateSelected = { date -> viewModel.seleccionarFecha(date) },
                viewModel = viewModel,
                onVistaMensualClick = onVistaMensualClick,
                eventos = eventos,
                tareas = tareas,
            )
            Spacer(modifier = Modifier.height(16.dp))

            // TodayEventsSection()

            // Spacer(modifier = Modifier.height(16.dp))

            // SaturdayEventsSection()

            weeklyEvents(viewModel)

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

        val pizarraFabActions =
            listOf(
                FabActionItem(
                    icon = Icons.Default.NoteAdd,
                    label = "Crear Post-it",
                    action = FabActionType.POST_IT,
                ),
            )
        val model =
            viewModel<DraggableViewModel>(
                key = "Home",
                factory = DraggableViewModelFactory("Home", casaId),
            )

        PizarraScreen(
            model,
            fabActions = pizarraFabActions,
            onFabActionSelected = { action ->
                when (action.action) {
                    FabActionType.POST_IT -> {
                        model.addNewPostIt()
                    }
                    else -> {}
                }
            },
        )
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
    onVistaMensualClick: () -> Unit,
    eventos: List<Evento>,
    tareas: List<Tarea>,
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
                text =
                    when (list.get(0).month.toString()) {
                        "JANUARY" -> "Enero"
                        "FEBRUARY" -> "Febrero"
                        "MARCH" -> "Marzo"
                        "APRIL" -> "Abril"
                        "MAY" -> "Mayo"
                        "JUNE" -> "Junio"
                        "JULY" -> "Julio"
                        "AUGUST" -> "Agosto"
                        "SEPTEMBER" -> "Septiembre"
                        "OCTOBER" -> "Octubre"
                        "NOVEMBER" -> "Noviembre"
                        else -> "Diciembre"
                    },
                color = Color.Black,
                style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.SemiBold),
            )

            Text(
                text = "Vista mensual",
                color = Color(0xff6c6c6c),
                textDecoration = TextDecoration.Underline,
                style = TextStyle(fontSize = 14.sp),
                modifier = Modifier.clickable { onVistaMensualClick() }, // Navegación activada
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            list.forEachIndexed { index, ldt ->
                val date = ldt.toLocalDate()
                val isSelected = selectedDate?.let { it == date } ?: false

                // Comprobamos si hay evento o tarea para este día
                val hasMarker =
                    remember(eventos, tareas, date) {
                        eventos.any { viewModel.parseFechaSegura(it.fechaInicio).isEqual(date) } ||
                            tareas.any { !it.fechaFin.isNullOrEmpty() && viewModel.parseFechaSegura(it.fechaFin!!).isEqual(date) }
                    }

                DayItem(
                    day = ldt.dayOfMonth.toString(),
                    dayName = viewModel.diasTraducidos(ldt.dayOfWeek),
                    isToday = index == 0,
                    isSelected = isSelected,
                    hasMarker = hasMarker,
                    onClick = { onDateSelected(date) },
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
    hasMarker: Boolean = false, // Nuevo parámetro
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

                // Indicador de evento/tarea (punto rojo dentro de la tarjeta)
                if (hasMarker) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Box(
                        modifier =
                            Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(Color(0xffff5686)),
                    )
                }
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
fun weeklyEvents(viewModel: HomeViewModel) {
    val eventosDelDia by viewModel.eventosDelDia.collectAsStateWithLifecycle()
    val sortedList = viewModel.sortEvents(eventosDelDia)
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
    ) {
        if (!sortedList.isEmpty()) {
            for (i in 0..<sortedList.size) {
                var prev = if (i == 0) null else sortedList.get(i - 1)
                var day = sortedList.get(i)
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
            val color: Color
            val textColor: Color
            if (tarea.prioridad == "Alta") {
                color = Color(0xFFFF6490)
                textColor = Color(0xFF581327)
            } else if (tarea.prioridad == "Media") {
                color = Color(0xFFDDC1FB)
                textColor = Color(0xFF5D427A)
            } else {
                color = Color(0xFFA9E6A8)
                textColor = Color(0xFF2D5C2C)
            }

            val date = viewModel.parseFechaSegura(tarea.fechaFin!!)
            val taskDate = viewModel.diasTraducidos(date.dayOfWeek) + ". " + date.dayOfMonth

            PendingTaskItem(
                title = tarea.nombre,
                priority = tarea.prioridad!!,
                priorityColor = color,
                textColor = textColor,
                taskDate,
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
