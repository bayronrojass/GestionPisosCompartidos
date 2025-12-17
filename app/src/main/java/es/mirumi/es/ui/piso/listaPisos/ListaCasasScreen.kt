package es.mirumi.es.ui.piso.listaPisos

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.zxing.integration.android.IntentIntegrator
import es.mirumi.es.R
import es.mirumi.es.data.SessionManager
import es.mirumi.es.data.remote.NetworkModule
import es.mirumi.es.data.repository.repositories.RepositoryCasa
import es.mirumi.es.model.Casa
import es.mirumi.es.model.requests.JoinCasaRequest
import es.mirumi.es.ui.navigation.Route
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

// Colores del tema
val PurplePrimary = Color(0xff8061a2)
val BackgroundColor = Color(0xfff8f8f8)
val TextBlack = Color.Black
val TextGray = Color(0xff6c6c6c)

@Composable
fun ListaCasasScreen(
    casas: List<Casa> = emptyList(),
    navController: NavHostController = rememberNavController(),
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context.applicationContext) }
    val repositoryCasa = remember { RepositoryCasa(NetworkModule.casaApiService) }

    // Launcher para QR Scanner
    val qrScannerLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult(),
        ) { result ->
            val intentResult = IntentIntegrator.parseActivityResult(result.resultCode, result.data)
            intentResult.contents?.let {
                handleQrResult(it, sessionManager, repositoryCasa, context)
            } ?: run {
                if (result.resultCode != Activity.RESULT_CANCELED) {
                    Toast.makeText(context, "Escaneo cancelado", Toast.LENGTH_SHORT).show()
                }
            }
        }

    Scaffold(
        containerColor = BackgroundColor,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Route.CrearPiso.route) },
                containerColor = Color.Black,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.padding(16.dp).size(60.dp),
            ) {
                Icon(Icons.Default.Add, contentDescription = "Crear nueva casa", modifier = Modifier.size(30.dp))
            }
        },
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp),
        ) {
            // 1. CABECERA
            Spacer(modifier = Modifier.height(25.dp))
            Text(
                text = "Mis pisos",
                style =
                    TextStyle(
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextBlack,
                    ),
                modifier = Modifier.padding(bottom = 20.dp),
            )

            // 2. BOTONES DE ACCIÓN RÁPIDA (Invitaciones y QR)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(15.dp),
            ) {
                // Botón Invitaciones
                ActionButton(
                    text = "Invitaciones",
                    iconRes = R.drawable.ic_email,
                    modifier = Modifier.weight(1f),
                    onClick = { navController.navigate(Route.Invitaciones.route) },
                )

                // Botón QR
                ActionButton(
                    text = "Escanear QR",
                    iconVector = Icons.Default.QrCodeScanner,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        iniciarEscanerQr(context, qrScannerLauncher)
                    },
                )
            }

            Spacer(modifier = Modifier.height(25.dp))

            // 3. LISTA DE PISOS
            if (casas.isEmpty()) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Image(
                            painter = painterResource(id = R.drawable.casita),
                            contentDescription = null,
                            modifier = Modifier.size(80.dp).alpha(0.3f),
                            colorFilter = ColorFilter.tint(TextGray),
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Aún no tienes pisos.\n¡Crea uno o únete con QR!",
                            style = TextStyle(fontSize = 16.sp, color = TextGray),
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 100.dp),
                ) {
                    items(casas, key = { it.id }) { casa ->
                        CasaItemModern(
                            casa = casa,
                            onClick = {
                                navController.navigate(
                                    Route.Home.createRoute(casa.id, casa.nombre),
                                )
                            },
                        )
                    }
                }
            }
        }
    }
}

// --- COMPONENTES UI ---

@Composable
private fun ActionButton(
    text: String,
    iconRes: Int? = null,
    iconVector: androidx.compose.ui.graphics.vector.ImageVector? = null,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Card(
        modifier =
            modifier
                .height(60.dp)
                .shadow(elevation = 2.dp, shape = RoundedCornerShape(15.dp))
                .clickable { onClick() },
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            if (iconRes != null) {
                Image(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    colorFilter = ColorFilter.tint(PurplePrimary),
                )
            } else if (iconVector != null) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = null,
                    tint = PurplePrimary,
                    modifier = Modifier.size(24.dp),
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                style =
                    TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextBlack,
                    ),
            )
        }
    }
}

@Composable
private fun CasaItemModern(
    casa: Casa,
    onClick: () -> Unit,
) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(90.dp)
                .shadow(elevation = 4.dp, shape = RoundedCornerShape(15.dp))
                .clickable { onClick() },
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f),
            ) {
                Box(
                    modifier =
                        Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(PurplePrimary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.iconopiso),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(PurplePrimary),
                        modifier = Modifier.size(26.dp),
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(verticalArrangement = Arrangement.Center) {
                    Text(
                        text = casa.nombre,
                        style =
                            TextStyle(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextBlack,
                            ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (casa.nombre.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = casa.nombre,
                            style =
                                TextStyle(
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = TextGray,
                                ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }

            Image(
                painter = painterResource(id = R.drawable.vector19),
                contentDescription = "Ir",
                colorFilter = ColorFilter.tint(PurplePrimary),
                modifier =
                    Modifier
                        .size(16.dp)
                        .rotate(180f),
            )
        }
    }
}

// --- LÓGICA DEL QR ---

// 1. Función de extensión para encontrar la Activity de forma segura
fun Context.findActivity(): Activity? =
    when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }

// 2. Función de inicio de escáner que usa la Activity
private fun iniciarEscanerQr(
    context: Context,
    launcher: ActivityResultLauncher<Intent>,
) {
    val activity = context.findActivity()
    if (activity != null) {
        val integrator =
            IntentIntegrator(activity).apply {
                setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
                setPrompt("Escanea el QR del piso para unirte")
                setBeepEnabled(false)
                setOrientationLocked(true) // Nota: Si esto falla en algunos dispositivos, ponlo a false
            }
        launcher.launch(integrator.createScanIntent())
    } else {
        Toast.makeText(context, "Error: No se pudo acceder a la cámara", Toast.LENGTH_SHORT).show()
    }
}

private fun handleQrResult(
    qrData: String,
    sessionManager: SessionManager,
    repositoryCasa: RepositoryCasa,
    context: Context,
) {
    try {
        val json = JSONObject(qrData)
        if (json.has("casaId")) {
            val casaId = json.getLong("casaId")
            val miId = sessionManager.fetchCurrentUserId()

            if (miId == -1L) {
                Toast.makeText(context, "Error: Inicia sesión antes de unirte", Toast.LENGTH_LONG).show()
                return
            }

            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val token = sessionManager.fetchAuthToken() ?: ""
                    val request = JoinCasaRequest(usuarioId = miId)
                    val response = repositoryCasa.joinCasa(token, casaId, request)

                    if (response.isSuccessful) {
                        Toast.makeText(context, "¡Te has unido al piso con éxito!", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(context, "Error al unirse: ${response.code()}", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Error de red: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        } else {
            Toast.makeText(context, "QR no válido", Toast.LENGTH_SHORT).show()
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Error al leer el QR", Toast.LENGTH_SHORT).show()
    }
}

// Función auxiliar para opacidad
fun Modifier.alpha(alpha: Float) =
    this.then(
        Modifier.drawWithContent {
            drawContent()
            drawRect(Color.White.copy(alpha = 1f - alpha), blendMode = BlendMode.DstIn)
        },
    )
