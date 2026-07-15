package es.mirumi.es.ui.piso.gestionUsuarios

import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import es.mirumi.es.data.SessionManager
import es.mirumi.es.data.remote.NetworkModule
import es.mirumi.es.data.repository.repositories.RepositoryCasa
import es.mirumi.es.data.repository.repositories.RepositoryInvitacion
import es.mirumi.es.ui.theme.Fondo
import es.mirumi.es.ui.theme.LilaPrimary
import es.mirumi.es.ui.theme.TextoGris
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Backward-compat aliases — the real values live in `ui/theme/AppColors.kt`.
val PurplePrimary = LilaPrimary
val BackgroundColor = Fondo
val TextBlack = Color.Black
val TextGray = TextoGris

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionUsuariosPiso(
    navController: NavController,
    pisoId: Long,
) {
    val context = LocalContext.current

    // Inicialización manual del ViewModel y sus dependencias
    val sessionManager = remember { SessionManager(context.applicationContext) }
    val pisoRepository = remember { RepositoryCasa(NetworkModule.casaApiService) }
    val invitacionRepository = remember { RepositoryInvitacion(NetworkModule.invitacionApiService) }

    val viewModel: GestionUsuariosPisoViewModel =
        viewModel(
            factory =
                GestionUsuariosPisoViewModelFactory(
                    pisoRepository,
                    invitacionRepository,
                    sessionManager,
                ),
        )

    // Cargamos los datos al entrar
    LaunchedEffect(pisoId) {
        viewModel.loadData(pisoId)
    }

    val miembros by viewModel.miembros.collectAsState()
    val accionResult by viewModel.accionResult.collectAsState()

    // Estado para el BottomSheet (Menú de invitación)
    var showInviteSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    // Manejo de mensajes (Toast)
    LaunchedEffect(accionResult) {
        accionResult?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            if (it.contains("enviada", ignoreCase = true)) {
                showInviteSheet = false // Cerrar si se envió correctamente
            }
            viewModel.clearAccionResult()
        }
    }

    Scaffold(
        containerColor = BackgroundColor,
        topBar = {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .padding(top = 40.dp, start = 20.dp, end = 20.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                // Botón atrás
                IconButton(
                    onClick = { navController.navigateUp() },
                    modifier = Modifier.size(40.dp),
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = TextGray)
                }

                Text(
                    text = "Miembros del piso",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextBlack),
                )
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showInviteSheet = true },
                containerColor = Color.Black,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.PersonAdd, null) },
                text = { Text("Invitar compañero") },
            )
        },
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp),
        ) {
            if (miembros.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay miembros cargados.", color = TextGray)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(15.dp),
                    contentPadding = PaddingValues(top = 10.dp, bottom = 100.dp),
                ) {
                    items(miembros) { miembro ->
                        UsuarioRow(miembro)
                    }
                }
            }
        }
    }

    if (showInviteSheet) {
        ModalBottomSheet(
            onDismissRequest = { showInviteSheet = false },
            sheetState = sheetState,
            containerColor = Color.White,
        ) {
            InvitacionBottomSheetContent(
                pisoId = pisoId,
                onSendEmail = { email -> viewModel.enviarInvitacion(email) },
            )
        }
    }
}

@Composable
fun UsuarioRow(miembro: MiembroPiso) {
    // Fila principal del usuario
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(Color(0xFFF8F8F8), shape = RoundedCornerShape(12.dp))
                .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // 1. EL "SÁNDWICH" (Letra de fondo + Foto por encima)
        Box(
            modifier =
                Modifier
                    .size(45.dp)
                    .clip(CircleShape)
                    .background(LilaPrimary),
            // Color de fondo si no hay foto
            contentAlignment = Alignment.Center,
        ) {
            // Capa de abajo: La letra (siempre se pinta por si la foto falla)
            Text(
                text = miembro.nombre.take(1).uppercase(),
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
            )

            if (!miembro.fotoUrl.isNullOrEmpty() && miembro.fotoUrl != "null") {
                SubcomposeAsyncImage(
                    model =
                        ImageRequest
                            .Builder(LocalContext.current)
                            .data("${miembro.fotoUrl}?v=${System.currentTimeMillis()}")
                            .crossfade(true)
                            .build(),
                    contentDescription = "Foto de perfil de ${miembro.nombre}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // 2. Nombre
        Text(
            text = miembro.nombre,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = Color.Black,
            modifier = Modifier.weight(1f),
        )

        // 3. Etiquetas (Admin / TÚ)
        if (miembro.esTu) {
            Text(
                text = "TÚ",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier =
                    Modifier
                        .background(Color(0xFFFF9800), shape = RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        if (miembro.esAdmin) {
            Text(
                text = "Admin.",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier =
                    Modifier
                        .background(Color(0xFF4CAF50), shape = RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
            )
        }
    }
}

@Composable
fun InvitacionBottomSheetContent(
    pisoId: Long,
    onSendEmail: (String) -> Unit,
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = QR, 1 = Email
    val tabs = listOf("Código QR", "Correo")

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(bottom = 50.dp),
    ) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.White,
            contentColor = PurplePrimary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = PurplePrimary,
                )
            },
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (index == 0) Icons.Default.QrCode else Icons.Default.Email,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(title, fontWeight = FontWeight.SemiBold)
                        }
                    },
                    selectedContentColor = PurplePrimary,
                    unselectedContentColor = TextGray,
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
            contentAlignment = Alignment.Center,
        ) {
            when (selectedTab) {
                0 -> QrTabContent(pisoId)
                1 -> EmailTabContent(onSendEmail)
            }
        }
    }
}

@Composable
fun QrTabContent(pisoId: Long) {
    val qrData = "mirumi://invite?casaId=$pisoId"
    val qrBitmap = rememberQrBitmap(content = qrData)

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Escanea para unirte",
            style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextBlack),
            modifier = Modifier.padding(bottom = 20.dp),
        )

        if (qrBitmap != null) {
            Box(
                modifier =
                    Modifier
                        .background(Color.White)
                        .padding(10.dp)
                        .border(2.dp, TextGray.copy(alpha = 0.2f), RoundedCornerShape(10.dp)),
            ) {
                Image(
                    bitmap = qrBitmap.asImageBitmap(),
                    contentDescription = "QR Code",
                    modifier = Modifier.size(230.dp),
                )
            }
        } else {
            CircularProgressIndicator(color = PurplePrimary)
        }

        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "Pídele a tu compañero que abra la app\ny escanee este código desde la lista de pisos.",
            textAlign = TextAlign.Center,
            color = TextGray,
            fontSize = 14.sp,
        )
    }
}

@Composable
fun EmailTabContent(onSend: (String) -> Unit) {
    var email by remember { mutableStateOf("") }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Enviar invitación por correo",
            style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextBlack),
            modifier = Modifier.padding(bottom = 20.dp),
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo electrónico") },
            placeholder = { Text("ejemplo@gmail.com") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors =
                OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PurplePrimary,
                    focusedLabelColor = PurplePrimary,
                ),
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = { onSend(email) },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
        ) {
            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text("Enviar Invitación")
        }
    }
}

@Composable
fun rememberQrBitmap(
    content: String,
    size: Int = 512,
): Bitmap? {
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(content) {
        withContext(Dispatchers.IO) {
            try {
                val bits =
                    QRCodeWriter().encode(
                        content,
                        BarcodeFormat.QR_CODE,
                        size,
                        size,
                        mapOf(EncodeHintType.MARGIN to 2),
                    )
                val w = bits.width
                val h = bits.height

                val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
                for (y in 0 until h) {
                    for (x in 0 until w) {
                        bmp.setPixel(
                            x,
                            y,
                            if (bits[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE,
                        )
                    }
                }
                bitmap = bmp
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    return bitmap
}
