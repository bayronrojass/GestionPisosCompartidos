package es.mirumi.es.ui.eventos

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

enum class PickerState { NONE, START, END }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventosScreen(viewModel: EventosViewModel) {
    var showDialog by remember { mutableStateOf(false) }
    var eventTitle by remember { mutableStateOf("") }
    var eventDescription by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf<LocalDateTime?>(null) }
    var endDate by remember { mutableStateOf<LocalDateTime?>(null) }
    var pickerState by remember { mutableStateOf(PickerState.NONE) }

    val context = LocalContext.current
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault()) }

    LaunchedEffect(pickerState) {
        when (pickerState) {
            PickerState.START -> {
                val calendar = Calendar.getInstance()
                val currentYear = calendar.get(Calendar.YEAR)
                val currentMonth = calendar.get(Calendar.MONTH)
                val currentDay = calendar.get(Calendar.DAY_OF_MONTH)

                DatePickerDialog(
                    context,
                    { _, year, month, dayOfMonth ->
                        val selectedDate = LocalDateTime.of(year, month + 1, dayOfMonth, 0, 0)
                        startDate = selectedDate
                        endDate = null
                        pickerState = PickerState.END
                    },
                    currentYear,
                    currentMonth,
                    currentDay,
                ).show()
            }

            PickerState.END -> {
                if (startDate == null) {
                    pickerState = PickerState.NONE
                    return@LaunchedEffect
                }

                val calendar = Calendar.getInstance()

                calendar.set(startDate!!.year, startDate!!.monthValue - 1, startDate!!.dayOfMonth)

                val currentYear = calendar.get(Calendar.YEAR)
                val currentMonth = calendar.get(Calendar.MONTH)
                val currentDay = calendar.get(Calendar.DAY_OF_MONTH)

                DatePickerDialog(
                    context,
                    { _, year, month, dayOfMonth ->
                        val selectedDate = LocalDateTime.of(year, month + 1, dayOfMonth, 0, 0)
                        endDate = selectedDate
                        pickerState = PickerState.NONE
                    },
                    currentYear,
                    currentMonth,
                    currentDay,
                ).apply {
                    val calendarMin =
                        Calendar.getInstance().apply {
                            set(
                                startDate!!.year,
                                startDate!!.monthValue - 1,
                                startDate!!.dayOfMonth,
                            )
                        }
                    datePicker.minDate = calendarMin.timeInMillis
                }.show()
            }

            PickerState.NONE -> {
            }
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Event")
            }
        },
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color.White),
        ) {
            // Header
            Text(
                text = "Calendario de eventos",
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )

            Card(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = MaterialTheme.shapes.medium,
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                ) {
                    // Month and Year Selectors
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                    ) {
                        // Month Spinner
                        var selectedMonth by remember { mutableStateOf("Enero") }
                        ExposedDropdownMenuBox(
                            expanded = false,
                            onExpandedChange = { },
                        ) {
                            Text(
                                text = selectedMonth,
                                modifier =
                                    Modifier
                                        .background(MaterialTheme.colorScheme.background)
                                        .padding(8.dp),
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Year Spinner
                        var selectedYear by remember { mutableStateOf("2024") }
                        ExposedDropdownMenuBox(
                            expanded = false,
                            onExpandedChange = { },
                        ) {
                            Text(
                                text = selectedYear,
                                modifier =
                                    Modifier
                                        .background(MaterialTheme.colorScheme.background)
                                        .padding(8.dp),
                            )
                        }
                    }

                    // Calendar Grid
                    CalendarGrid(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                    )

                    // Buttons
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        TextButton(onClick = { /* Handle cancel */ }) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        TextButton(onClick = { /* Handle OK */ }) {
                            Text("OK")
                        }
                    }
                }
            }

            // Events List
            EventsList(
                modifier =
                    Modifier
                        .weight(1f)
                        .padding(16.dp),
                viewModel = viewModel,
            )
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = {
                    showDialog = false
                    // Reset states when dialog is dismissed
                    pickerState = PickerState.NONE
                },
                title = { Text("¡Crea un evento!") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = eventTitle,
                            onValueChange = { eventTitle = it },
                            label = { Text("Título del evento") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = eventDescription,
                            onValueChange = { eventDescription = it },
                            label = { Text("Descripción") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 3,
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            "Fechas del evento",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(bottom = 8.dp),
                        )

                        Button(
                            onClick = {
                                pickerState = PickerState.START
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Icon(
                                Icons.Default.CalendarMonth,
                                contentDescription = "Seleccionar fechas",
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                if (startDate != null && endDate != null) {
                                    "${startDate!!.format(dateFormatter)} - ${
                                        endDate!!.format(
                                            dateFormatter,
                                        )
                                    }"
                                } else if (startDate != null) {
                                    "${startDate!!.format(dateFormatter)} - Seleccionar fecha fin"
                                } else {
                                    "Seleccionar rango de fechas"
                                },
                            )
                        }

                        if (startDate != null && endDate != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Evento: ${startDate!!.format(dateFormatter)} al ${
                                    endDate!!.format(
                                        dateFormatter,
                                    )
                                }",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (eventTitle.isNotBlank() && startDate != null && endDate != null) {
                                viewModel.crea(
                                    title = eventTitle,
                                    description = eventDescription,
                                    startDate = startDate!!,
                                    endDate = endDate!!,
                                )
                                showDialog = false
                                eventTitle = ""
                                eventDescription = ""
                                startDate = null
                                endDate = null
                                pickerState = PickerState.NONE
                            }
                        },
                        enabled = eventTitle.isNotBlank() && startDate != null && endDate != null,
                    ) {
                        Text("Guardar")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showDialog = false
                            eventTitle = ""
                            eventDescription = ""
                            startDate = null
                            endDate = null
                            pickerState = PickerState.NONE
                        },
                    ) {
                        Text("Cancelar")
                    }
                },
            )
        }
    }
}

@Composable
fun CalendarGrid(modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        // Day headers
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            listOf("D", "L", "M", "X", "J", "V", "S").forEach { day ->
                Text(
                    text = day,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(4.dp),
                )
            }
        }

        // Calendar days
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.height(200.dp),
        ) {
            items(42) { index ->
                Box(
                    modifier =
                        Modifier
                            .aspectRatio(1f)
                            .padding(2.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = MaterialTheme.shapes.small,
                            ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = "${index + 1}")
                }
            }
        }
    }
}

@Composable
fun EventsList(
    modifier: Modifier = Modifier,
    viewModel: EventosViewModel,
) {
    val events by viewModel.events.collectAsState()

    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
    ) {
        if (events.isEmpty()) {
            Text(
                "No hay eventos creados",
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            events.forEach { event ->
                EventCard(
                    event = event,
                    modifier = Modifier.padding(vertical = 4.dp),
                )
            }
        }
    }
}

@Composable
fun EventCard(
    event: Event,
    modifier: Modifier = Modifier,
) {
    val formatter = remember { java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = event.title,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
            )
            Text(
                text = event.description,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
            Text(
                text = "${formatter.format(event.startDate)} - ${formatter.format(event.endDate)}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}
