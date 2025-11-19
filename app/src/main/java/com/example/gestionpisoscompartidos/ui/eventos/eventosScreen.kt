package com.example.gestionpisoscompartidos.ui.eventos

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventosScreen(viewModel: eventosViewModel) {
    var showDialog by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var eventTitle by remember { mutableStateOf("") }
    var eventDescription by remember { mutableStateOf("") }
    var pickedDate by remember { mutableStateOf<Date?>(null) }

    val context = LocalContext.current

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
                            // Simplified dropdown implementation
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
            )
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("¡Crea un evento!") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = eventTitle,
                            onValueChange = { eventTitle = it },
                            label = { Text("Título del evento") },
                            modifier = Modifier.fillMaxWidth(),
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

                        Text("Fecha", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                        Button(
                            onClick = {
                                showDatePickerDialog(context, initialDate = pickedDate) { date ->
                                    pickedDate = date
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = "Seleccionar fecha")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                pickedDate?.let {
                                    java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it)
                                } ?: "Seleccionar fecha",
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (eventTitle.isNotBlank() && pickedDate != null) {
                                val event =
                                    Event(
                                        id = System.currentTimeMillis().toString(),
                                        title = eventTitle,
                                        description = eventDescription,
                                        date = "",
                                        time = "",
                                    )
                                showDialog = false
                                eventTitle = ""
                                eventDescription = ""
                            }
                        },
                        enabled = eventTitle.isNotBlank() && pickedDate != null,
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
                        },
                    ) {
                        Text("Cancelar")
                    }
                },
            )
        }
    }
}

private fun showDatePickerDialog(
    context: Context,
    initialDate: Date? = null,
    onDateSelected: (Date) -> Unit,
) {
    val calendar = Calendar.getInstance()

    if (initialDate != null) {
        calendar.time = initialDate
    }
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    val datePickerDialog =
        android.app.DatePickerDialog(
            context,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.set(selectedYear, selectedMonth, selectedDay)
                onDateSelected(selectedCalendar.time)
            },
            year,
            month,
            day,
        )
    datePickerDialog.show()
}

@Composable
fun CalendarGrid(modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
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

        LazyVerticalGrid(
            columns =
                androidx.compose.foundation.lazy.grid.GridCells
                    .Fixed(7),
            modifier = Modifier.height(200.dp),
        ) {
            items(42) { index ->
                // 6 weeks
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
fun EventsList(modifier: Modifier = Modifier) {
    Column(
        modifier =
            modifier
                .verticalScroll(rememberScrollState()),
    ) {
        // Example events - replace with actual data from ViewModel
        repeat(10) { index ->
            EventCard(
                title = "Evento ${index + 1}",
                description = "Descripción del evento",
                modifier = Modifier.padding(vertical = 4.dp),
            )
        }
    }
}

@Composable
fun EventCard(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
            )
            Text(
                text = description,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
