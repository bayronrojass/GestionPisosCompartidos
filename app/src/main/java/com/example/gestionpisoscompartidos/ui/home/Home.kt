package com.example.gestionpisoscompartidos.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gestionpisoscompartidos.R
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import java.time.LocalDateTime

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel,
) {
    val userName by viewModel.userName.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoadingUser.collectAsStateWithLifecycle()

    val houseName by viewModel.currentHouse.collectAsStateWithLifecycle()

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

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(color = Color(0xfff8f8f8))
                .verticalScroll(rememberScrollState()),
    ) {
        HeaderSection(userName = userName, houseName)

        Spacer(modifier = Modifier.height(16.dp))

        CalendarSection(next7days)

        Spacer(modifier = Modifier.height(16.dp))

        TodayEventsSection()

        Spacer(modifier = Modifier.height(16.dp))

        SaturdayEventsSection()

        Spacer(modifier = Modifier.height(24.dp))

        NotificationsSection()

        Spacer(modifier = Modifier.height(24.dp))

        PendingTasksSection()

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "ver más",
            color = Color(0xff6c6c6c),
            textDecoration = TextDecoration.Underline,
            style = TextStyle(fontSize = 14.sp),
            modifier = Modifier.align(alignment = androidx.compose.ui.Alignment.CenterHorizontally),
        )

        Spacer(modifier = Modifier.height(80.dp)) // Space for navbar
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
fun CalendarSection(list: List<LocalDateTime>) {
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
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            list.forEachIndexed { index, ldt ->
                DayItem(
                    day = ldt.dayOfMonth.toString(),
                    dayName =
                        when (ldt.dayOfWeek.toString()) {
                            "MONDAY" -> "Lun"
                            "TUESDAY" -> "Mar"
                            "WEDNESDAY" -> "Mié"
                            "THURSDAY" -> "Jue"
                            "FRIDAY" -> "Vie"
                            "SATURDAY" -> "Sáb"
                            else -> "Dom"
                        },
                    isToday = index == 0,
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
                    containerColor = if (isToday) Color(0xfffeee91) else Color.White,
                ),
            border = CardDefaults.outlinedCardBorder(),
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
fun TodayEventsSection() {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
    ) {
        Text(
            text = "Hoy",
            color = Color.Black,
            style =
                TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                ),
        )

        Spacer(modifier = Modifier.height(12.dp))

        EventItem(
            title = "Cenan en casa María y Belén",
            color = Color(0xfffff8cf),
        )
    }
}

@Composable
fun SaturdayEventsSection() {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
    ) {
        Text(
            text = "Sábado",
            color = Color.Black,
            style =
                TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                ),
        )

        Spacer(modifier = Modifier.height(12.dp))

        EventItem(
            title = "Viene el casero a arreglar la nevera",
            color = Color.White,
        )

        Spacer(modifier = Modifier.height(8.dp))

        EventItem(
            title = "Previa con los bros en casa :p",
            color = Color.White,
        )
    }
}

@Composable
fun EventItem(
    title: String,
    color: Color,
) {
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
fun PendingTasksSection() {
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

        PendingTaskItem(
            title = "Tirar basura",
            priority = "Media",
            priorityColor = Color(0xffddc1fb),
            textColor = Color(0xff5d427a),
        )

        Spacer(modifier = Modifier.height(12.dp))

        PendingTaskItem(
            title = "Limpiar baño",
            priority = "Alta",
            priorityColor = Color(0xffff6490),
            textColor = Color(0xff581327),
        )
    }
}

@Composable
fun PendingTaskItem(
    title: String,
    priority: String,
    priorityColor: Color,
    textColor: Color,
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

                // Priority tag
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
                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    placeholder = { Text("14 Nov") },
                    modifier = Modifier.weight(1f),
                    colors =
                        TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                        ),
                )

                // Change request
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
