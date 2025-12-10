package es.mirumi.es.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import es.mirumi.es.R
import es.mirumi.es.model.Evento
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month
import java.time.Year
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun CalendarioScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: HomeViewModel,
) {
    LaunchedEffect(Unit) {
        viewModel.cargarEventos()
        viewModel.cargarEventosDelMes()
    }

    val eventosDelMes by viewModel.eventosDelMes.collectAsStateWithLifecycle()

    val date = LocalDateTime.now()

    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val isFloatingButtonVisible =
        remember {
            derivedStateOf { scrollState.value > 100 }
        }

    var showEventDialog by remember { mutableStateOf(false) }
    var editingEvent by remember { mutableStateOf<Evento?>(null) }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Color(0xfff8f8f8))
                .padding(16.dp)
                .verticalScroll(scrollState),
    ) {
        TopBar(onNavigateBack)

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = viewModel.mesesTraducidos(date.month.toString()),
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )

        Spacer(modifier = Modifier.height(16.dp))

        CalendarGrid(
            eventos = eventosDelMes,
            viewModel = viewModel,
            month = date.month,
            year = date.year,
        )

        Spacer(modifier = Modifier.height(24.dp))

        EventsSection(
            eventos = eventosDelMes,
            viewModel = viewModel,
            date = date,
            onEditEvent = { event ->
                editingEvent = event
                showEventDialog = true
            },
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

    EventDialog(
        showDialog = showEventDialog,
        event = editingEvent,
        onDismiss = { showEventDialog = false },
        onConfirm = { nombre, descripcion, fechaInicio, fechaFin ->
            if (editingEvent != null) {
                if (fechaInicio == null || fechaFin == null) {
                    return@EventDialog
                }
                viewModel.actualizarEvento(
                    evento = editingEvent!!,
                    nuevoNombre = nombre,
                    nuevaDescripcion = descripcion,
                    nuevaFechaInicio = fechaInicio.atStartOfDay(),
                    nuevaFechaFin = fechaFin.atStartOfDay(),
                )
            } else {
                val startDateTime = fechaInicio?.atStartOfDay() ?: LocalDate.now().atStartOfDay()
                val endDateTime = fechaFin?.atStartOfDay() ?: startDateTime
                viewModel.crea(
                    title = nombre,
                    description = descripcion,
                    startDate = startDateTime,
                    endDate = endDateTime,
                )
            }
            showEventDialog = false
        },
        viewModel = viewModel,
    )

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(bottom = 24.dp),
        contentAlignment = Alignment.CenterEnd,
    ) {
        AddEventFloatingButton(
            onClick = {
                editingEvent = null
                showEventDialog = true
            },
            modifier =
                Modifier.offset(
                    x = (-10).dp,
                    y = 80.dp,
                ),
        )
    }
}

@Composable
fun TopBar(onBackClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
    ) {
      Image(
          painter = painterResource(id = R.drawable.icono_atr_s),
          contentDescription = "Back",
          modifier =
                Modifier
                    .size(24.dp)
                    .clickable { onBackClick() },
       )

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "Calendario",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xff6c6c6c),
        )
    }
}

/* ============================
   CALENDAR GRID
   ============================ */

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CalendarGrid(
    eventos: List<Evento>,
    viewModel: HomeViewModel,
    month: Month,
    year: Int,
) {
    val daysOfWeek = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")

    val yearInt = year.toInt()
    val yearObj = Year.of(yearInt)
    val daysInMonth = month.length(yearObj.isLeap)

    val firstDayOfMonth = LocalDate.of(yearInt, month, 1)
    val firstWeekday = firstDayOfMonth.dayOfWeek.value - 1

    val totalCells = 42
    val blankCellsBefore = firstWeekday
    val days = (1..daysInMonth).toList()
    val hoy = LocalDate.now()

    Column {
        Row(modifier = Modifier.fillMaxWidth()) {
            daysOfWeek.forEach { day ->
                Text(
                    text = day,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(300.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(count = blankCellsBefore) {
                Box(modifier = Modifier.size(45.dp))
            }

            items(days) { day ->
                val fechaDelDia = LocalDate.of(year, month, day)

                val hasEvent =
                    eventos.any {
                        viewModel.parseFechaSegura(it.fechaInicio) == fechaDelDia
                    }

                CalendarDay(
                    day = day,
                    isToday = fechaDelDia == hoy,
                    hasEvent = hasEvent,
                )
            }

            val remainingCells = totalCells - blankCellsBefore - daysInMonth
            items(count = remainingCells) {
                Box(modifier = Modifier.size(45.dp))
            }
        }
    }
}

/* ============================
   CALENDAR DAY CELL
   ============================ */

@Composable
fun CalendarDay(
    day: Int,
    isToday: Boolean,
    hasEvent: Boolean,
) {
    val bg = if (isToday) Color(0xfffeee91) else Color.White

    Box(
        modifier = Modifier.size(45.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier =
                Modifier
                    .matchParentSize()
                    .clip(CircleShape)
                    .background(bg)
                    .border(1.dp, Color(0xffe9e3e3), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = day.toString(),
                fontSize = 16.sp,
                color = Color.Black,
            )
        }

        if (hasEvent) {
            Box(
                modifier =
                    Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(Color(0xffff5686))
                        .align(Alignment.TopEnd)
                        .offset(x = 4.dp, y = (-4).dp),
            )
        }
    }
}

@Composable
fun EventsSection(
    eventos: List<Evento>,
    viewModel: HomeViewModel,
    date: LocalDateTime,
    onEditEvent: (Evento) -> Unit,
) {
    Column {
        Text(
            text = "EVENTOS",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
        )

        Spacer(modifier = Modifier.height(12.dp))

        val agrupados =
            eventos
                .groupBy {
                    viewModel.parseFechaSegura(it.fechaInicio)
                }.toSortedMap()

        if (agrupados.isNotEmpty()) {
            var previousMonth: Month? = null

            agrupados.keys.forEach { fecha ->
                val currentMonth = fecha.month

                if (previousMonth != currentMonth) {
                    Text(
                        text = viewModel.mesesTraducidos(currentMonth.toString()),
                        color = Color.Black,
                        style =
                            TextStyle(
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Medium,
                            ),
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    previousMonth = currentMonth
                }
            }
        }

        agrupados.forEach { (fecha, eventosDia) ->
            val today = date.toLocalDate()

            Text(
                text = "${viewModel.diasTraducidos(fecha.dayOfWeek)} ${fecha.dayOfMonth}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
            )

            Spacer(modifier = Modifier.height(8.dp))

            eventosDia.forEach { evento ->
                val fechaEvento = viewModel.parseFechaSegura(evento.fechaInicio)
                EventItem(
                    text = evento.nombre,
                    highlighted = fechaEvento == today,
                    event = evento,
                    viewModel = viewModel,
                    onEditClick = { onEditEvent(evento) },
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun EventItem(
    text: String,
    highlighted: Boolean,
    event: Evento,
    viewModel: HomeViewModel,
    onEditClick: () -> Unit,
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    val background = if (highlighted) Color(0xfffff8cf) else Color.White

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(background)
                .padding(12.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color(0xffff5686)),
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = text,
            fontSize = 15.sp,
            color = Color.Black,
            modifier = Modifier.weight(1f),
        )

        IconButton(
            onClick = { showDeleteDialog = true },
            modifier = Modifier.size(24.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = "Eliminar evento",
                tint = Color.Unspecified,
            )
        }

        IconButton(
            onClick = { onEditClick() },
            modifier = Modifier.size(24.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.Edit,
                contentDescription = "Editar evento",
                tint = Color.Unspecified,
            )
        }
    }

    DeleteEventDialog(
        showDialog = showDeleteDialog,
        onDismiss = { showDeleteDialog = false },
        onConfirm = {
            viewModel.eliminar(event.id!!)
            showDeleteDialog = false
        },
    )
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
fun DeleteEventDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = "Eliminar evento",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )
            },
            text = {
                Text(
                    text = "¿Estás seguro de que quieres eliminar el evento? Esta acción no se podrá revertir.",
                    fontSize = 14.sp,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = onConfirm,
                    colors =
                        ButtonDefaults.textButtonColors(
                            contentColor = Color.Red,
                        ),
                ) {
                    Text("Eliminar", fontWeight = FontWeight.Medium)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onDismiss,
                    colors =
                        ButtonDefaults.textButtonColors(
                            contentColor = Color.Black,
                        ),
                ) {
                    Text("Cancelar")
                }
            },
            shape = RoundedCornerShape(12.dp),
            containerColor = Color.White,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDialog(
    showDialog: Boolean,
    event: Evento? = null,
    onDismiss: () -> Unit,
    onConfirm: (String, String, LocalDate?, LocalDate?) -> Unit,
    viewModel: HomeViewModel,
) {
    if (!showDialog) return

    val isEditing = event != null
    val dialogTitle = if (isEditing) "Editar evento" else "Crear evento"
    val confirmButtonText = if (isEditing) "Guardar" else "Crear"

    var eventName by remember { mutableStateOf(event?.nombre ?: "") }
    var eventDescription by remember { mutableStateOf(event?.descripcion ?: "") }
    var nameError by remember { mutableStateOf(false) }
    var dateError by remember { mutableStateOf(false) } // New error state for dates

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    var startDate by remember { mutableStateOf<LocalDate?>(null) }
    var endDate by remember { mutableStateOf<LocalDate?>(null) }

    LaunchedEffect(isEditing) {
        if (isEditing && event != null) {
            startDate = viewModel.parseFechaSegura(event.fechaInicio)
            endDate = viewModel.parseFechaSegura(event.fechaFin)
        } else {
            startDate = null
            endDate = null
        }
    }

    var isSelectingDates by remember { mutableStateOf(false) }

    var tempStartDate by remember { mutableStateOf<LocalDate?>(null) }
    var tempEndDate by remember { mutableStateOf<LocalDate?>(null) }

    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault())
    val accentColor = Color(0xFFFF5686)

    fun startDateSelectionFlow() {
        isSelectingDates = true
        tempStartDate = startDate
        tempEndDate = null
        showStartDatePicker = true
    }

    fun onStartDateSelected(date: LocalDate) {
        startDate = date
        showStartDatePicker = false
        tempEndDate = date
        showEndDatePicker = true
    }

    fun onEndDateSelected(date: LocalDate) {
        endDate = if (date.isBefore(startDate!!)) startDate else date
        showEndDatePicker = false
        isSelectingDates = false
    }

    val isFormValid by remember {
        derivedStateOf {
            eventName.isNotBlank() && startDate != null && endDate != null
        }
    }

    // ---------------------------- MAIN DIALOG ----------------------------

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(dialogTitle, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                OutlinedTextField(
                    value = eventName,
                    onValueChange = {
                        eventName = it
                        if (nameError && it.isNotBlank()) nameError = false
                    },
                    label = { Text("Nombre del evento") },
                    isError = nameError,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                if (nameError) {
                    Text("El nombre no puede estar vacío", color = Color.Red, fontSize = 12.sp)
                }

                OutlinedTextField(
                    value = eventDescription,
                    onValueChange = { eventDescription = it },
                    label = { Text("Descripción") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            "Fechas del evento",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (dateError) Color.Red else Color.Gray,
                        )
                        Text(
                            "*",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Red,
                        )
                    }

                    Button(
                        onClick = { startDateSelectionFlow() },
                        enabled = !isSelectingDates,
                        modifier = Modifier.fillMaxWidth(),
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = accentColor,
                                contentColor = Color.White,
                            ),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CalendarMonth,
                                contentDescription = "Select dates",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp),
                            )
                            Column(
                                horizontalAlignment = Alignment.Start,
                                verticalArrangement = Arrangement.spacedBy(2.dp),
                            ) {
                                Text(
                                    text =
                                        when {
                                            startDate != null && endDate != null -> "Fechas seleccionadas"
                                            isSelectingDates -> "Seleccionando fechas..."
                                            else -> "Seleccionar fechas del evento"
                                        },
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                )
                                if (startDate != null || endDate != null) {
                                    Text(
                                        text =
                                            buildString {
                                                if (startDate != null) {
                                                    append(
                                                        "Inicio: ${
                                                            startDate!!.format(
                                                                dateFormatter,
                                                            )
                                                        }",
                                                    )
                                                }
                                                if (startDate != null && endDate != null) append(" | ")
                                                if (endDate != null) {
                                                    append("Fin: ${endDate!!.format(dateFormatter)}")
                                                }
                                            },
                                        color = Color.White.copy(alpha = 0.9f),
                                        fontSize = 12.sp,
                                    )
                                } else {
                                    Text(
                                        text = "Por favor, selecciona las fechas",
                                        color = Color.White.copy(alpha = 0.9f),
                                        fontSize = 12.sp,
                                    )
                                }
                            }
                        }
                    }

                    // Show date error message
                    if (dateError) {
                        Text(
                            "Por favor, selecciona las fechas del evento",
                            color = Color.Red,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 4.dp),
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    var hasError = false

                    if (eventName.isBlank()) {
                        nameError = true
                        hasError = true
                    }

                    if (startDate == null || endDate == null) {
                        dateError = true
                        hasError = true
                    }

                    if (hasError) return@TextButton

                    onConfirm(eventName, eventDescription, startDate, endDate)
                    onDismiss()
                },
                enabled = isFormValid,
                colors =
                    ButtonDefaults.textButtonColors(
                        contentColor = if (isFormValid) accentColor else Color.Gray,
                    ),
            ) {
                Text(
                    confirmButtonText,
                    color = if (isFormValid) accentColor else Color.Gray,
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = Color.Gray),
            ) { Text("Cancelar") }
        },
        shape = RoundedCornerShape(12.dp),
        containerColor = Color.White,
    )

    // ---------------------------- START DATE PICKER ----------------------------

    if (showStartDatePicker) {
        DatePickerDialog(
            onDismissRequest = {
                showStartDatePicker = false
                isSelectingDates = false
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        tempStartDate?.let {
                            onStartDateSelected(it)
                            dateError = false
                        }
                    },
                    colors =
                        ButtonDefaults.textButtonColors(
                            contentColor = accentColor,
                        ),
                ) {
                    Text("Continuar", fontWeight = FontWeight.Medium)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showStartDatePicker = false
                        isSelectingDates = false
                    },
                    colors =
                        ButtonDefaults.textButtonColors(
                            contentColor = Color.Gray,
                        ),
                ) { Text("Cancelar") }
            },
        ) {
            val initialDate = tempStartDate ?: LocalDate.now()
            key(initialDate) {
                Column {
                    Text(
                        text = "Selecciona la fecha de inicio del evento",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )

                    Text(
                        text = "No puedes seleccionar fechas anteriores a hoy",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp),
                    )

                    Divider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = Color.LightGray.copy(alpha = 0.5f),
                    )

                    val pickerState =
                        rememberDatePickerState(
                            initialSelectedDate = initialDate,
                            initialDisplayMode = DisplayMode.Picker,
                            initialDisplayedMonth = YearMonth.from(initialDate),
                            selectableDates =
                                object : SelectableDates {
                                    override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                                        val selectedDate =
                                            Instant
                                                .ofEpochMilli(utcTimeMillis)
                                                .atZone(ZoneId.systemDefault())
                                                .toLocalDate()
                                        return !selectedDate.isBefore(LocalDate.now())
                                    }

                                    override fun isSelectableYear(year: Int): Boolean = year >= LocalDate.now().year
                                },
                        )

                    DatePicker(
                        state = pickerState,
                        title = null,
                        showModeToggle = false,
                        colors =
                            DatePickerDefaults.colors(
                                titleContentColor = Color.Black,
                                headlineContentColor = Color.Black,
                                weekdayContentColor = Color.Black,
                                subheadContentColor = Color.Black,
                                navigationContentColor = accentColor,
                                yearContentColor = Color.Black,
                                disabledYearContentColor = Color.LightGray,
                                currentYearContentColor = accentColor,
                                selectedYearContentColor = Color.White,
                                disabledSelectedYearContentColor = Color.White.copy(alpha = 0.5f),
                                selectedYearContainerColor = accentColor,
                                disabledSelectedYearContainerColor = accentColor.copy(alpha = 0.5f),
                                dayContentColor = Color.Black,
                                disabledDayContentColor = Color.LightGray.copy(alpha = 0.5f),
                                selectedDayContentColor = Color.White,
                                disabledSelectedDayContentColor = Color.White.copy(alpha = 0.5f),
                                selectedDayContainerColor = accentColor,
                                disabledSelectedDayContainerColor = accentColor.copy(alpha = 0.5f),
                                todayContentColor = accentColor,
                                todayDateBorderColor = accentColor,
                                dayInSelectionRangeContentColor = Color.Black,
                                dayInSelectionRangeContainerColor = accentColor.copy(alpha = 0.2f),
                                dividerColor = Color.LightGray,
                            ),
                    )

                    LaunchedEffect(pickerState.selectedDateMillis) {
                        tempStartDate =
                            pickerState.selectedDateMillis?.let {
                                Instant
                                    .ofEpochMilli(it)
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDate()
                            }
                    }
                }
            }
        }
    }

    // ---------------------------- END DATE PICKER ----------------------------

    if (showEndDatePicker) {
        DatePickerDialog(
            onDismissRequest = {
                showEndDatePicker = false
                isSelectingDates = false
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        tempEndDate?.let {
                            onEndDateSelected(it)
                            dateError = false
                        }
                    },
                    colors =
                        ButtonDefaults.textButtonColors(
                            contentColor = accentColor,
                        ),
                ) {
                    Text("Seleccionar", fontWeight = FontWeight.Medium)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showEndDatePicker = false
                        isSelectingDates = false
                    },
                    colors =
                        ButtonDefaults.textButtonColors(
                            contentColor = Color.Gray,
                        ),
                ) { Text("Cancelar") }
            },
        ) {
            val initial = tempEndDate ?: startDate ?: LocalDate.now()
            key(initial) {
                Column {
                    Text(
                        text = "Selecciona la fecha de fin del evento",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )

                    if (startDate != null) {
                        Text(
                            text = "Fecha de inicio: ${startDate!!.format(dateFormatter)}",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        )
                    }

                    Text(
                        text = "No puedes seleccionar fechas anteriores a la fecha de inicio",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp),
                    )

                    Divider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = Color.LightGray.copy(alpha = 0.5f),
                    )

                    val pickerState =
                        rememberDatePickerState(
                            initialSelectedDate = initial,
                            initialDisplayMode = DisplayMode.Picker,
                            initialDisplayedMonth = YearMonth.from(initial),
                            selectableDates =
                                object : SelectableDates {
                                    override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                                        val selectedDate =
                                            Instant
                                                .ofEpochMilli(utcTimeMillis)
                                                .atZone(ZoneId.systemDefault())
                                                .toLocalDate()
                                        return startDate?.let { !selectedDate.isBefore(it) } ?: true
                                    }

                                    override fun isSelectableYear(year: Int): Boolean = startDate?.let { year >= it.year } ?: true
                                },
                        )

                    DatePicker(
                        state = pickerState,
                        title = null,
                        showModeToggle = false,
                        colors =
                            DatePickerDefaults.colors(
                                titleContentColor = Color.Black,
                                headlineContentColor = Color.Black,
                                weekdayContentColor = Color.Black,
                                subheadContentColor = Color.Black,
                                navigationContentColor = accentColor,
                                yearContentColor = Color.Black,
                                disabledYearContentColor = Color.LightGray,
                                currentYearContentColor = accentColor,
                                selectedYearContentColor = Color.White,
                                disabledSelectedYearContentColor = Color.White.copy(alpha = 0.5f),
                                selectedYearContainerColor = accentColor,
                                disabledSelectedYearContainerColor = accentColor.copy(alpha = 0.5f),
                                dayContentColor = Color.Black,
                                disabledDayContentColor = Color.LightGray.copy(alpha = 0.5f),
                                selectedDayContentColor = Color.White,
                                disabledSelectedDayContentColor = Color.White.copy(alpha = 0.5f),
                                selectedDayContainerColor = accentColor,
                                disabledSelectedDayContainerColor = accentColor.copy(alpha = 0.5f),
                                todayContentColor = accentColor,
                                todayDateBorderColor = accentColor,
                                dayInSelectionRangeContentColor = Color.Black,
                                dayInSelectionRangeContainerColor = accentColor.copy(alpha = 0.2f),
                                dividerColor = Color.LightGray,
                            ),
                    )

                    LaunchedEffect(pickerState.selectedDateMillis) {
                        tempEndDate =
                            pickerState.selectedDateMillis?.let {
                                Instant
                                    .ofEpochMilli(it)
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDate()
                            }
                    }
                }
            }
        }
    }
}

@Composable
fun AddEventFloatingButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = Color(0xFFFF5686),
        contentColor = Color.White,
        modifier = modifier,
    ) {
        Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = "Add Event",
            modifier = Modifier.size(24.dp),
        )
    }
}
