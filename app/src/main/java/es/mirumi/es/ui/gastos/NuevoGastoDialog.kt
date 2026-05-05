package es.mirumi.es.ui.gastos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import es.mirumi.es.model.Gasto
import es.mirumi.es.model.dtos.BorradorGastoDTO

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NuevoGastoDialog(
    onDismiss: () -> Unit,
    gastoEditar: Gasto? = null,
    borradorInicial: BorradorGastoDTO? = null,
    miembrosCasa: List<UsuarioPiso>,
    onConfirm: (String, String, String, List<String>, Long) -> Unit,
) {
    var nombre by remember { mutableStateOf(borradorInicial?.concepto ?: gastoEditar?.nombre ?: "") }
    var importe by remember { mutableStateOf(borradorInicial?.total?.toString() ?: gastoEditar?.importe?.toString() ?: "") }
    var categoriaSelected by remember { mutableStateOf(gastoEditar?.categoria ?: "COMIDA") }

    // BENEFICIARIOS (Los que participan en el gasto) -> Guardamos sus NOMBRES
    var beneficiariosSelected by remember {
        mutableStateOf(
            gastoEditar?.beneficiarios?.takeIf { it.isNotEmpty() }?.toSet()
                ?: miembrosCasa.map { it.nombre }.toSet(),
        )
    }
    // PAGADOR (El que puso el dinero) -> Guardamos su ID
    var pagadorIdSelected by remember {
        mutableStateOf(
            gastoEditar?.let { g -> miembrosCasa.find { it.nombre == g.pagadoPorNombre }?.id }
                ?: miembrosCasa.firstOrNull()?.id ?: 0L,
        )
    }

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
                // TICKET ESCANEADO
                if (borradorInicial?.urlTicket != null) {
                    AsyncImage(
                        model = borradorInicial.urlTicket,
                        contentDescription = "Ticket escaneado",
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.LightGray),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    )
                }

                // NOMBRE
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre del gasto") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                )

                // IMPORTE
                OutlinedTextField(
                    value = importe,
                    onValueChange = { importe = it },
                    label = { Text("Importe (€)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                )

                // CATEGORÍA
                Text("Categoría:", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CategoryChip("COMIDA", categoriaSelected) { categoriaSelected = it }
                    CategoryChip("OCIO", categoriaSelected) { categoriaSelected = it }
                    CategoryChip("OTROS", categoriaSelected) { categoriaSelected = it }
                }

                // 🔥 NUEVA SECCIÓN: QUIÉN PAGA (CHIPS VERDES - SELECCIÓN ÚNICA)
                Text("¿Quién lo ha pagado?", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    miembrosCasa.forEach { miembro ->
                        val isSelected = pagadorIdSelected == miembro.id
                        FilterChip(
                            selected = isSelected,
                            onClick = { pagadorIdSelected = miembro.id },
                            label = { Text(miembro.nombre) },
                            colors =
                                FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF00E676), // Verde Bizum
                                    containerColor = ColorFondo,
                                ),
                        )
                    }
                }

                // BENEFICIARIOS (CHIPS LILAS - SELECCIÓN MÚLTIPLE)
                Text("¿Para quién es?", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    miembrosCasa.forEach { usuario ->
                        val nombreMiembro = usuario.nombre
                        val isSelected = beneficiariosSelected.contains(nombreMiembro)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                val current = beneficiariosSelected.toMutableSet()
                                if (isSelected) {
                                    if (current.size > 1) current.remove(nombreMiembro)
                                } else {
                                    current.add(nombreMiembro)
                                }
                                beneficiariosSelected = current
                            },
                            label = { Text(nombreMiembro) },
                            leadingIcon =
                                if (isSelected) {
                                    { Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) }
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
                        onConfirm(nombre, importe, categoriaSelected, beneficiariosSelected.toList(), pagadorIdSelected)
                        onDismiss()
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.Black),
            ) {
                Text("Aceptar", color = Color.Black)
            }
        },
    )
}
