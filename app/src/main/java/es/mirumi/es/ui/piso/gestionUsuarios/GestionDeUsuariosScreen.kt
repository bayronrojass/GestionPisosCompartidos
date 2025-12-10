package es.mirumi.es.ui.piso.gestionUsuarios

import android.content.Context
import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import es.mirumi.es.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionUsuariosPisoScreen(
    viewModel: GestionUsuariosPisoViewModel = viewModel(),
    pisoId: Long = 0L,
    onBack: () -> Unit,
) {
    var showEmailDialog by remember { mutableStateOf(false) }
    var showQrDialog by remember { mutableStateOf(false) }
    var showRemoveDialog by remember { mutableStateOf<MiembroPiso?>(null) }
    var emailInput by remember { mutableStateOf("") }

    val miembros by viewModel.miembros.collectAsState()
    val accionResult by viewModel.accionResult.collectAsState()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(pisoId) {
        if (pisoId != 0L) {
            viewModel.loadData(pisoId)
        } else {
            Toast
                .makeText(context, "Error FATAL: ID de piso no encontrado", Toast.LENGTH_LONG)
                .show()
        }
    }

    LaunchedEffect(accionResult) {
        accionResult?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            viewModel.clearAccionResult()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(R.drawable.ic_menu),
                            contentDescription = "Menu",
                            tint = Color.White,
                            modifier =
                                Modifier
                                    .size(30.dp)
                                    .clickable { onBack() },
                        )
                        Spacer(Modifier.width(16.dp))
                        Text(
                            "Matías Perelló 15", // Esto debería venir del ViewModel
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF6200EE),
                        titleContentColor = Color.White,
                    ),
                actions = {
                    Icon(
                        painter = painterResource(R.drawable.ic_user),
                        contentDescription = "Profile",
                        tint = Color.White,
                        modifier =
                            Modifier
                                .size(30.dp)
                                .clickable { /* Abrir perfil */ },
                    )
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .background(Color.White),
        ) {
            Text(
                "Miembros",
                color = Color(0xFF6A5ACD),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
                textAlign = TextAlign.Center,
            )

            // Lista de miembros
            LazyColumn(
                modifier =
                    Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp, vertical = 16.dp),
            ) {
                val currentListas = miembros

                items(
                    count = currentListas.size,
                    key = { index -> currentListas[index].id },
                ) { index ->
                    val miembro = currentListas[index]
                    MiembroItem(
                        miembro = miembro,
                        onRemoveClick = { showRemoveDialog = miembro },
                    )
                }
            }

            // Botones de invitación
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
            ) {
                Button(
                    onClick = { showEmailDialog = true },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
//                    colors =
//                        ButtonDefaults.buttonColors(
//                            backgroundColor = Color.White,
//                        ),
                    elevation = ButtonDefaults.buttonElevation(4.dp),
                ) {
                    Text(
                        "Invitar por Email",
                        color = Color.Black,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        painter = painterResource(R.drawable.ic_email),
                        contentDescription = null,
                        tint = Color.Black,
                    )
                }

                Button(
                    onClick = {
                        if (pisoId == 0L) {
                            Toast
                                .makeText(
                                    context,
                                    "Error: No se ha cargado el ID del piso",
                                    Toast.LENGTH_SHORT,
                                ).show()
                        } else {
                            scope.launch {
                                try {
                                    val qrData = "{\"action\":\"join_casa\", \"casaId\":$pisoId}"
                                    val qrBitmap = generarQrBitmap(qrData, context)
                                    // Mostrar diálogo QR (necesitarías convertir Bitmap a ImageBitmap)
                                    showQrDialog = true
                                } catch (e: Exception) {
                                    Toast
                                        .makeText(
                                            context,
                                            "Error al generar QR",
                                            Toast.LENGTH_SHORT,
                                        ).show()
                                }
                            }
                        }
                    },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp),
//                    colors =
//                        ButtonDefaults.buttonColors(
//                            backgroundColor = Color.White,
//                        ),
                    elevation = ButtonDefaults.buttonElevation(4.dp),
                ) {
                    Text(
                        "Invitar usando QR",
                        color = Color.Black,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        painter = painterResource(R.drawable.ic_qr_code),
                        contentDescription = null,
                        tint = Color.Black,
                    )
                }
            }
        }
    }

    // Diálogo para invitar por email
    if (showEmailDialog) {
        AlertDialog(
            onDismissRequest = { showEmailDialog = false },
            title = { Text("Invitar por Email") },
            text = {
                Column {
                    Text("Introduce el email del nuevo miembro:")
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        placeholder = { Text("email@ejemplo.com") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions =
                            KeyboardOptions.Default.copy(
                                keyboardType = KeyboardType.Email,
                            ),
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (emailInput.isNotBlank()) {
                            scope.launch {
                                viewModel.enviarInvitacion(emailInput)
                            }
                            showEmailDialog = false
                            emailInput = ""
                        } else {
                            Toast
                                .makeText(
                                    context,
                                    "El email no puede estar vacío",
                                    Toast.LENGTH_SHORT,
                                ).show()
                        }
                    },
                ) {
                    Text("Enviar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showEmailDialog = false
                        emailInput = ""
                    },
                ) {
                    Text("Cancelar")
                }
            },
        )
    }

    // Diálogo de confirmación para eliminar
    showRemoveDialog?.let { miembro ->
        AlertDialog(
            onDismissRequest = { showRemoveDialog = null },
            title = { Text("Confirmar eliminación") },
            text = { Text("¿Estás seguro de que quieres eliminar a ${miembro.nombre} del piso?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            viewModel.removeMiembro(miembro.id)
                        }
                        showRemoveDialog = null
                    },
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showRemoveDialog = null },
                ) {
                    Text("Cancelar")
                }
            },
        )
    }

    // Diálogo para mostrar QR (simplificado - necesitarías implementar la generación de QR en Compose)
    if (showQrDialog) {
        AlertDialog(
            onDismissRequest = { showQrDialog = false },
            title = { Text("Invitar con QR") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text("Pídele a tu amigo que escanee este código.")
                    Spacer(Modifier.height(16.dp))
                    // Aquí iría el Image del QR generado
                    Box(
                        modifier =
                            Modifier
                                .size(200.dp)
                                .background(Color.LightGray),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("QR Code Placeholder")
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showQrDialog = false },
                ) {
                    Text("Cerrar")
                }
            },
        )
    }
}

@Composable
fun MiembroItem(
    miembro: MiembroPiso,
    onRemoveClick: () -> Unit,
) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(4.dp),
//        backgroundColor = Color.White,
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = miembro.nombre,
                fontSize = 18.sp,
                color = Color.Black,
            )

            IconButton(onClick = onRemoveClick) {
                Icon(
                    painter = painterResource(R.drawable.ic_red_cancel),
                    contentDescription = "Eliminar miembro",
                    tint = Color.Red,
                )
            }
        }
    }
}

// Función auxiliar para generar QR (similar a la original)
private suspend fun generarQrBitmap(
    data: String,
    context: Context,
): Bitmap? =
    withContext(Dispatchers.IO) {
        try {
            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, 512, 512)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bmp = createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bmp[x, y] =
                        if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE
                }
            }
            bmp
        } catch (e: Exception) {
            null
        }
    }
