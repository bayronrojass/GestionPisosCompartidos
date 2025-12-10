package es.mirumi.es.ui.gastos

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NuevoGastoDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    // Estado local del formulario
    var nombreGasto by remember { mutableStateOf("Nombre Gasto") }
    var tipoGasto by remember { mutableStateOf(0) } // 0: Fijo, 1: Puntual

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth(0.95f), // Ocupar casi todo el ancho
        containerColor = Color.White,
        title = null,
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Cabecera con X de cerrar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(nombreGasto, fontSize = 22.sp, color = ColorTextoGris)
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = ColorTextoGris, modifier = Modifier.size(20.dp))
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = ColorTextoGris)
                    }
                }

                // 1. Tipo de Gasto
                Text("Tipo de gasto:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = tipoGasto == 0,
                        onClick = { tipoGasto = 0 },
                        label = { Text("Fijo") },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = ColorLila),
                    )
                    FilterChip(
                        selected = tipoGasto == 1,
                        onClick = { tipoGasto = 1 },
                        label = { Text("Puntual") },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = ColorLila),
                    )
                }

                // 2. Fecha / Frecuencia (Cambia según el tipo)
                if (tipoGasto == 0) {
                    Text("Frecuencia del gasto:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SuggestionChip(
                            onClick = {},
                            label = { Text("Cada mes") },
                            colors = SuggestionChipDefaults.suggestionChipColors(containerColor = ColorLila),
                        )
                        SuggestionChip(onClick = {}, label = { Text("+ Otra") })
                    }
                } else {
                    Text("Fecha del gasto:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    OutlinedButton(
                        onClick = { /* DatePicker */ },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp),
                    ) {
                        Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("18 de noviembre", color = ColorTextoGris)
                    }
                }

                // 3. Recordar Gasto
                Text("Recordar gasto:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SuggestionChip(onClick = {}, label = { Text("No") })
                    SuggestionChip(
                        onClick = {},
                        label = { Text("1 semana antes") },
                        colors = SuggestionChipDefaults.suggestionChipColors(containerColor = ColorLila),
                    )
                    SuggestionChip(onClick = {}, label = { Text("+ Otra") })
                }

                // 4. Compartir con
                Text("Compartir gasto con:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    // Chips de personas simulados
                    AssistChip(onClick = {}, label = { Text("Daniel") }, leadingIcon = { Icon(Icons.Default.Check, null) })
                    AssistChip(onClick = {}, label = { Text("Raquel") }, leadingIcon = { Icon(Icons.Default.Add, null) })
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Botón Aceptar
                Button(
                    onClick = onConfirm,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color.Black),
                ) {
                    Text("Aceptar", color = Color.Black)
                }
            }
        },
        confirmButton = {}, // Lo hemos puesto dentro del content para personalizarlo
    )
}
