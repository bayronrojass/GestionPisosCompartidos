package es.mirumi.es.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import es.mirumi.es.model.Evento
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun CalendarioFullView(
    fechaSeleccionada: LocalDate,
    eventos: List<Evento>,
    onFechaClick: (LocalDate) -> Unit,
    onBackClick: () -> Unit,
    onAddEventClick: () -> Unit = {},
) {
    val viewModel: HomeViewModel = viewModel()

    var showEditDialog by remember { mutableStateOf(false) }
    var eventoToEdit by remember { mutableStateOf<Evento?>(null) }

    // Function to open edit dialog
    fun openEditDialog(evento: Evento) {
        eventoToEdit = evento
        showEditDialog = true
    }

    val currentMonth = YearMonth.from(fechaSeleccionada)
    val daysInMonth = currentMonth.lengthOfMonth()
    val firstDayOfWeek = currentMonth.atDay(1).dayOfWeek.value
    val daysList = (1 until firstDayOfWeek).map { null } + (1..daysInMonth).map { it }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddEventClick,
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
                text = currentMonth.month.getDisplayName(TextStyle.FULL, Locale("es", "ES")).replaceFirstChar { it.uppercase() },
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

            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                contentPadding = PaddingValues(vertical = 8.dp),
            ) {
                items(daysList) { day ->
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
                                Text(
                                    text = day.toString(),
                                    fontSize = 16.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = Color.Black,
                                )
                                if (hasEvent) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(Color(0xffff5686)))
                                }
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.padding(4.dp).aspectRatio(1f))
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text("Eventos", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))

            val eventosDelDiaSeleccionado =
                eventos.filter {
                    parsearFechaSegura(it.fechaInicio).isEqual(fechaSeleccionada)
                }

            ListaEventosDelDia(
                eventos = eventosDelDiaSeleccionado,
                viewModel = viewModel,
                onEditEvent = { evento -> openEditDialog(evento) },
            )
        }
    }

    if (showEditDialog) {
        eventoToEdit?.let { evento ->
            AlertDialog(
                onDismissRequest = { showEditDialog = false },
                title = { Text("Editar Evento") },
                text = { Text("Funcionalidad de edición no implementada") },
                confirmButton = {
                    TextButton(onClick = { showEditDialog = false }) {
                        Text("OK")
                    }
                },
            )
        }
    }
}
