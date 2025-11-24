package com.example.gestionpisoscompartidos.ui.home

import android.app.DatePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gestionpisoscompartidos.model.Evento
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle as JavaTextStyle
import java.util.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePickerDefaults.dateFormatter
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedTextField
import androidx.lifecycle.viewmodel.compose.viewModel
import java.time.ZoneId
import java.time.format.DateTimeFormatter

val ColorRosaFuerte = Color(0xffff5686)
val ColorFondoGris = Color(0xfff8f8f8)
val ColorAmarilloNota = Color(0xfffff8cf)

enum class PickerState { NONE, START, END }

@Composable
fun SelectorDiasSemana(
    fechaSeleccionada: LocalDate,
    onFechaClick: (LocalDate) -> Unit,
    onVistaMensualClick: () -> Unit,
    viewModel: HomeViewModel,
) {
    val listaDias = (0..30).map { LocalDate.now().plusDays(it.toLong()) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = fechaSeleccionada.month.getDisplayName(JavaTextStyle.FULL, Locale("es", "ES")).uppercase(),
                style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black),
            )
            Text(
                text = "Vista mensual",
                style = TextStyle(fontSize = 13.sp, textDecoration = TextDecoration.Underline, color = Color.Black),
                modifier = Modifier.clickable { onVistaMensualClick() },
            )
        }

        // Fila de días
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(listaDias) { fecha ->
                ItemDiaCalendario(
                    fecha = fecha,
                    isSelected = fecha.isEqual(fechaSeleccionada),
                    onClick = { onFechaClick(fecha) },
                )
            }
        }
    }
}

@Composable
fun CalendarioFullView(
    fechaSeleccionada: LocalDate,
    eventos: List<Evento>,
    onFechaClick: (LocalDate) -> Unit,
    onBackClick: () -> Unit,
    viewModel: HomeViewModel,
) {
    val context = LocalContext.current

    var showDialog by remember { mutableStateOf(false) }
    var nuevoNombre by remember { mutableStateOf("") }
    var nuevaDescripcion by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf<LocalDate?>(null) }
    var endDate by remember { mutableStateOf<LocalDate?>(null) }

    val currentMonth = YearMonth.from(fechaSeleccionada)
    val daysInMonth = currentMonth.lengthOfMonth()
    val firstDayOfWeek = currentMonth.atDay(1).dayOfWeek.value
    val daysList = (1 until firstDayOfWeek).map { null } + (1..daysInMonth).map { it }

    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    showDialog = true
                    startDate = null
                    endDate = null
                },
                containerColor = Color.Black,
                contentColor = Color.White,
                shape = CircleShape,
            ) {
                Icon(Icons.Default.Add, contentDescription = "Añadir Evento")
            }
        },
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF8F8F8))
                    .padding(padding)
                    .padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(text = "Calendario", color = Color.Gray, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = currentMonth.month.getDisplayName(JavaTextStyle.FULL, Locale("es", "ES")).replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
            Spacer(modifier = Modifier.height(20.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                listOf("Lun", "Mar", "Mie", "Jue", "Vie", "Sáb", "Dom").forEach { day ->
                    Text(
                        text = day,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp,
                        color = Color.Gray,
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LazyVerticalGrid(columns = GridCells.Fixed(7)) {
                items(daysList.size) { index ->
                    val day = daysList[index]
                    if (day != null) {
                        val date = currentMonth.atDay(day)
                        val isSelected = date.isEqual(fechaSeleccionada)
                        val hasEvent =
                            eventos.any {
                                parsearFechaSegura(it.fechaInicio).isEqual(date)
                            }

                        Box(
                            modifier =
                                Modifier
                                    .padding(4.dp)
                                    .aspectRatio(1f)
                                    .clip(CircleShape)
                                    .background(if (isSelected) Color(0xfffff8cf) else Color.Transparent)
                                    .clickable { onFechaClick(date) },
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = day.toString())
                                if (hasEvent) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(ColorRosaFuerte))
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text("Eventos", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))

            val eventosDelDiaSeleccionado =
                eventos.filter {
                    parsearFechaSegura(it.fechaInicio).isEqual(fechaSeleccionada)
                }

            ListaEventosDelDia(
                eventosDelDiaSeleccionado,
                viewModel,
            )
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
                showDialog = false
                nuevoNombre = ""
                nuevaDescripcion = ""
                startDate = null
                endDate = null
            },
            confirmButton = {
                TextButton(
                    enabled = nuevoNombre.isNotBlank() && startDate != null && endDate != null,
                    onClick = {
                        startDate?.let { start ->
                            endDate?.let { end ->
                                viewModel.crea(
                                    title = nuevoNombre,
                                    description = nuevaDescripcion,
                                    startDate = start.atStartOfDay(),
                                    endDate = end.atStartOfDay(),
                                )
                                showDialog = false
                                nuevoNombre = ""
                                nuevaDescripcion = ""
                                startDate = null
                                endDate = null
                            }
                        }
                    },
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDialog = false
                    nuevoNombre = ""
                    nuevaDescripcion = ""
                    startDate = null
                    endDate = null
                }) {
                    Text("Cancelar")
                }
            },
            title = { Text("Nuevo Evento") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = nuevoNombre,
                        onValueChange = { nuevoNombre = it },
                        label = { Text("Nombre") },
                        isError = nuevoNombre.isBlank(),
                    )
                    OutlinedTextField(
                        value = nuevaDescripcion,
                        onValueChange = { nuevaDescripcion = it },
                        label = { Text("Descripción") },
                    )

                    Button(
                        onClick = {
                            val initialStartDate = startDate ?: fechaSeleccionada
                            DatePickerDialog(
                                context,
                                { _, year, month, day ->
                                    val selectedStartDate = LocalDate.of(year, month + 1, day)
                                    startDate = selectedStartDate

                                    val initialEndDate = endDate ?: selectedStartDate
                                    DatePickerDialog(
                                        context,
                                        { _, endYear, endMonth, endDay ->
                                            val selectedEndDate = LocalDate.of(endYear, endMonth + 1, endDay)
                                            endDate = selectedEndDate
                                        },
                                        selectedStartDate.year,
                                        selectedStartDate.monthValue - 1,
                                        selectedStartDate.dayOfMonth,
                                    ).apply {
                                        datePicker.minDate =
                                            selectedStartDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                                    }.show()
                                },
                                initialStartDate.year,
                                initialStartDate.monthValue - 1,
                                initialStartDate.dayOfMonth,
                            ).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = "Seleccionar rango de fechas")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (startDate != null && endDate != null) {
                                "${startDate!!.format(dateFormatter)} - ${endDate!!.format(dateFormatter)}"
                            } else if (startDate != null) {
                                "${startDate!!.format(dateFormatter)} - Seleccionar fecha fin"
                            } else {
                                "Seleccionar rango de fechas"
                            },
                        )
                    }

                    if (startDate != null && endDate != null) {
                        Text(
                            text = "Rango seleccionado: ${startDate!!.format(dateFormatter)} - ${endDate!!.format(dateFormatter)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                        )
                    } else if (startDate != null) {
                        Text(
                            text = "Selecciona la fecha de fin",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                        )
                    } else {
                        Text(
                            text = "Haz clic para seleccionar las fechas",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                        )
                    }
                }
            },
        )
    }
}

@Composable
fun ItemDiaCalendario(
    fecha: LocalDate,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val colorBorde = if (isSelected) ColorRosaFuerte else Color(0xffd9d9d9)
    val colorFondo = if (isSelected) ColorRosaFuerte.copy(alpha = 0.1f) else Color(0xfffffefe)

    Box(
        modifier =
            Modifier
                .width(45.dp)
                .height(70.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(colorFondo)
                .border(BorderStroke(1.dp, colorBorde), RoundedCornerShape(20.dp))
                .clickable { onClick() },
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize().padding(top = 10.dp),
        ) {
            Text(
                text = fecha.dayOfMonth.toString(),
                style =
                    TextStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isSelected) ColorRosaFuerte else Color.Black,
                    ),
            )
            Text(
                text =
                    fecha.dayOfWeek
                        .getDisplayName(JavaTextStyle.SHORT, Locale("es", "ES"))
                        .replace(".", "")
                        .replaceFirstChar { it.uppercase() },
                style = TextStyle(fontSize = 12.sp, color = Color.Gray),
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

@Composable
fun ListaEventosDelDia(
    eventos: List<Evento>,
    viewModel: HomeViewModel,
) {
    Column(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (eventos.isEmpty()) {
            Text(
                text = "No hay eventos para este día",
                style = TextStyle(fontSize = 14.sp, color = Color.Gray),
                modifier = Modifier.padding(start = 40.dp, top = 10.dp),
            )
        } else {
            eventos.forEach { evento ->
                ItemEventoTimeline(
                    evento = evento,
                    onDeleteEvent = { eventId -> viewModel.onDeleteEvent(eventId) },
                )
            }
        }
    }
}

@Composable
fun ItemEventoTimeline(
    evento: Evento,
    onDeleteEvent: (Long) -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(30.dp),
        ) {
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(ColorRosaFuerte))
        }
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(ColorAmarilloNota)
                    .shadow(elevation = 4.dp, shape = RoundedCornerShape(10.dp))
                    .padding(horizontal = 15.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            Column {
                Text(text = evento.nombre, style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color.Black))
                if (!evento.descripcion.isNullOrBlank()) {
                    Text(text = evento.descripcion, style = TextStyle(fontSize = 12.sp, color = Color.Gray))
                }
            }

            IconButton(
                onClick = { onDeleteEvent(evento.id!!) },
                modifier = Modifier.align(Alignment.CenterEnd),
            ) {
                Icon(Icons.Filled.Delete, contentDescription = "Eliminar")
            }
        }
    }
}

@Composable
fun ListaEventosAgrupados(
    eventos: List<Evento>,
    viewModel: HomeViewModel,
) {
    val eventosPorFecha =
        eventos.groupBy {
            try {
                LocalDate.parse(it.fechaInicio.take(10))
            } catch (e: Exception) {
                LocalDate.now()
            }
        }

    Column(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (eventos.isEmpty()) {
            Text(
                text = "No hay más eventos esta semana",
                style = TextStyle(fontSize = 14.sp, color = Color.Gray),
                modifier = Modifier.padding(start = 40.dp, top = 10.dp),
            )
        } else {
            eventosPorFecha.forEach { (fecha, lista) ->

                val tituloCabecera =
                    when {
                        fecha.isEqual(LocalDate.now()) -> "Hoy"
                        fecha.isEqual(LocalDate.now().plusDays(1)) -> "Mañana"
                        else -> {
                            val diaSemana =
                                fecha.dayOfWeek
                                    .getDisplayName(java.time.format.TextStyle.SHORT, Locale("es", "ES"))
                                    .replace(".", "")
                                    .replaceFirstChar { it.uppercase() }
                            val diaMes = fecha.dayOfMonth
                            "$diaSemana $diaMes"
                        }
                    }

                Text(
                    text = tituloCabecera,
                    style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black),
                    modifier = Modifier.padding(top = 8.dp),
                )

                lista.forEach { evento ->
                    ItemEventoTimeline(
                        evento = evento,
                        onDeleteEvent = { eventId -> viewModel.onDeleteEvent(eventId) },
                    )
                }
            }
        }
    }
}

fun parsearFechaSegura(fechaString: String): LocalDate =
    try {
        java.time.LocalDateTime
            .parse(fechaString, java.time.format.DateTimeFormatter.ISO_DATE_TIME)
            .toLocalDate()
    } catch (e: Exception) {
        try {
            LocalDate.parse(fechaString.take(10))
        } catch (e2: Exception) {
            LocalDate.now()
        }
    }
