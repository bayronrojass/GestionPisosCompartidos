package es.mirumi.es.ui.gastos

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import es.mirumi.es.model.Gasto
import es.mirumi.es.model.dtos.BorradorGastoDTO
import java.util.Locale
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NuevoGastoDialog(
    onDismiss: () -> Unit,
    gastoEditar: Gasto? = null,
    borradorInicial: BorradorGastoDTO? = null,
    miembrosCasa: List<UsuarioPiso>,
    onConfirm: (String, String, String, List<String>, Map<Long, Double>) -> Unit,
) {
    var nombre by remember { mutableStateOf(borradorInicial?.concepto ?: gastoEditar?.nombre ?: "") }
    var importe by remember { mutableStateOf(borradorInicial?.total?.toString() ?: gastoEditar?.importe?.toString() ?: "") }
    var categoriaSelected by remember { mutableStateOf(gastoEditar?.categoria ?: "COMIDA") }

    // Tipado explícito a Set<String>
    var beneficiariosSelected by remember {
        mutableStateOf<Set<String>>(
            gastoEditar?.beneficiarios?.takeIf { it.isNotEmpty() }?.toSet()
                ?: miembrosCasa.map { it.nombre }.toSet(),
        )
    }

    var pagadoresMap by remember {
        mutableStateOf<Map<Long, Double>>(
            gastoEditar?.aportaciones?.associate { it.usuarioId to it.cantidad } ?: emptyMap(),
        )
    }

    // CÁLCULOS DINÁMICOS PARA LA UI
    val importeTotal = importe.replace(',', '.').toDoubleOrNull() ?: 0.0
    val totalPagado = pagadoresMap.values.sum()
    val diferencia = importeTotal - totalPagado

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
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // TICKET ESCANEADO
                if (borradorInicial?.urlTicket != null) {
                    item {
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
                }

                // NOMBRE
                item {
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        label = { Text("Nombre del gasto") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                    )
                }

                // IMPORTE TOTAL
                item {
                    OutlinedTextField(
                        value = importe,
                        onValueChange = { importe = it },
                        label = { Text("Importe Total (€)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                    )
                }

                // CATEGORÍA
                item {
                    Text("Categoría:", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    val todasLasCategorias = listOf("ALQUILER", "COMIDA", "SUMINISTROS", "OCIO", "TRANSPORTE", "OTROS")
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        todasLasCategorias.forEach { cat ->
                            CategoryChip(cat, categoriaSelected) { categoriaSelected = it }
                        }
                    }
                }

                // QUIÉN LO HA PAGADO Y CUÁNTO
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("¿Quién lo ha pagado?", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)

                        // Botón mágico para repartir a medias el pago
                        TextButton(
                            onClick = {
                                if (importeTotal > 0 && miembrosCasa.isNotEmpty()) {
                                    val aPagarPorCabeza = Math.round((importeTotal / miembrosCasa.size) * 100.0) / 100.0
                                    pagadoresMap = miembrosCasa.associate { it.id to aPagarPorCabeza }
                                }
                            },
                            contentPadding = PaddingValues(0.dp),
                        ) {
                            Text("A partes iguales", color = ColorMoradoOscuro, fontSize = 12.sp, textDecoration = TextDecoration.Underline)
                        }
                    }
                }

                // Lista de usuarios con Inputs
                items(miembrosCasa) { miembro ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(miembro.nombre, modifier = Modifier.weight(1f), fontSize = 16.sp)

                        val montoActual =
                            pagadoresMap[miembro.id]?.let {
                                if (it == 0.0) "" else String.format(Locale.US, "%.2f", it)
                            } ?: ""

                        OutlinedTextField(
                            value = montoActual,
                            onValueChange = { text ->
                                val limpio = text.replace(',', '.')
                                val nuevoMapa = pagadoresMap.toMutableMap()
                                val valor = limpio.toDoubleOrNull()
                                if (valor != null && valor > 0) {
                                    nuevoMapa[miembro.id] = valor
                                } else {
                                    nuevoMapa.remove(miembro.id)
                                }
                                pagadoresMap = nuevoMapa
                            },
                            label = { Text("0.00 €") },
                            modifier = Modifier.width(120.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(8.dp),
                        )
                    }
                }

                // INDICADOR VISUAL DE CUÁNTO FALTA O SOBRA
                item {
                    val colorMensaje =
                        when {
                            diferencia > 0.01 -> ColorRojoSaldo // Falta dinero
                            diferencia < -0.01 -> Color(0xFFFFA000) // Sobra dinero
                            else -> ColorVerdeSaldo // Cuadra perfecto
                        }

                    val textoMensaje =
                        when {
                            diferencia > 0.01 -> "Falta asignar: ${String.format("%.2f", diferencia)}€"
                            diferencia < -0.01 -> "Sobran: ${String.format("%.2f", abs(diferencia))}€"
                            else -> "¡Las cuentas cuadran! (0.00€)"
                        }

                    Text(
                        text = textoMensaje,
                        color = colorMensaje,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End,
                    )
                }

                // BENEFICIARIOS
                item {
                    Spacer(modifier = Modifier.height(8.dp))
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
            }
        },
        confirmButton = {
            val context = LocalContext.current
            Button(
                onClick = {
                    if (nombre.isEmpty() || importeTotal <= 0) {
                        Toast.makeText(context, "Introduce un nombre e importe válidos", Toast.LENGTH_SHORT).show()
                    } else if (pagadoresMap.isEmpty()) {
                        Toast.makeText(context, "Debes indicar al menos un pagador", Toast.LENGTH_SHORT).show()
                    } else if (abs(diferencia) > 0.05) {
                        Toast.makeText(context, "Los pagos no cuadran con el total.", Toast.LENGTH_SHORT).show()
                    } else {
                        onConfirm(nombre, importeTotal.toString(), categoriaSelected, beneficiariosSelected.toList(), pagadoresMap)
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
