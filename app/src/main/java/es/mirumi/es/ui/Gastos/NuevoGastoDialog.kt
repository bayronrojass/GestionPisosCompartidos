package es.mirumi.es.ui.gastos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.mirumi.es.model.Gasto

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NuevoGastoDialog(
    onDismiss: () -> Unit,
    gastoEditar: Gasto? = null,
    miembrosCasa: List<String>, // Nueva lista de miembros para elegir
    onConfirm: (String, String, String, List<String>) -> Unit, // Ahora devuelve la lista de beneficiarios
) {
    var nombre by remember { mutableStateOf(gastoEditar?.nombre ?: "") }
    var importe by remember { mutableStateOf(gastoEditar?.importe?.toString() ?: "") }
    var categoriaSelected by remember { mutableStateOf(gastoEditar?.categoria ?: "OTROS") }

    // Por defecto, todos están seleccionados
    var beneficiariosSelected by remember { mutableStateOf(miembrosCasa.toSet()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    if (gastoEditar == null) "Nuevo Gasto" else "Editar Gasto",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorTextoGris,
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = ColorTextoGris)
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre del gasto") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                )

                OutlinedTextField(
                    value = importe,
                    onValueChange = { importe = it },
                    label = { Text("Importe (€)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                )

                Text("Categoría:", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CategoryChip("COMIDA", categoriaSelected) { categoriaSelected = it }
                    CategoryChip("OCIO", categoriaSelected) { categoriaSelected = it }
                    CategoryChip("OTROS", categoriaSelected) { categoriaSelected = it }
                }

                // NUEVA SECCIÓN: Selección de beneficiarios
                Text("¿Para quién es?", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    miembrosCasa.forEach { miembro ->
                        val isSelected = beneficiariosSelected.contains(miembro)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                val current = beneficiariosSelected.toMutableSet()
                                if (isSelected) {
                                    if (current.size > 1) current.remove(miembro) // Evitar dejarlo vacío
                                } else {
                                    current.add(miembro)
                                }
                                beneficiariosSelected = current
                            },
                            label = { Text(miembro) },
                            leadingIcon =
                                if (isSelected) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else {
                                    null
                                },
                            colors =
                                FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ColorLila,
                                    containerColor = ColorFondo,
                                ),
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nombre.isNotEmpty() && importe.isNotEmpty()) {
                        onConfirm(nombre, importe, categoriaSelected, beneficiariosSelected.toList())
                        onDismiss()
                    }
                },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.Black),
            ) {
                Text("Aceptar", color = Color.Black)
            }
        },
    )
}
