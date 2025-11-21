package com.example.gestionpisoscompartidos.ui.eventos

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.util.Date
import com.example.gestionpisoscompartidos.R

@Composable
fun EventDialogComposeContent(
    viewModel: EventDialogViewModel,
    pickedDate: Date?,
    onDateSelected: (Date) -> Unit,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var eventTitle by remember { mutableStateOf("") }
    var eventDescription by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }

    val isFormValid = viewModel.buttonState(eventTitle, pickedDate)

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Título",
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
        )
        OutlinedTextField(
            value = eventTitle,
            onValueChange = { eventTitle = it },
            placeholder = { Text("Título") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            isError = eventTitle.isBlank(),
            supportingText = {
                if (eventTitle.isBlank()) {
                    Text("Introduzca un título para el evento")
                }
            },
        )

        Text(
            text = "Descripción",
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
        )
        OutlinedTextField(
            value = eventDescription,
            onValueChange = { eventDescription = it },
            placeholder = { Text("Descripción") },
            singleLine = false,
            maxLines = 3,
            modifier = Modifier.fillMaxWidth(),
        )

        Text(
            text = "Fecha",
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
        )
        Button(
            onClick = { showDatePicker = true },
            modifier = Modifier.wrapContentWidth(),
        ) {
            Icon(
                painter = painterResource(R.drawable.outline_calendar_month_24),
                contentDescription = "Seleccionar fecha",
            )
            Spacer(Modifier.width(8.dp))
            Text(
                pickedDate?.let {
                    java.text.DateFormat
                        .getDateInstance(java.text.DateFormat.MEDIUM)
                        .format(it)
                } ?: "Seleccionar fecha",
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = { onConfirm(eventTitle) },
            ) {
                Text("Guardar")
            }
        }
    }

    if (showDatePicker) {
        MaterialDatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            onDateSelected = { date ->
                onDateSelected(Date(date))
                showDatePicker = false
            },
            initialDate = pickedDate?.time,
        )
    }
}
