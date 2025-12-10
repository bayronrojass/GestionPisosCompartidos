package es.mirumi.es.ui.piso.listaPisos

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.zxing.integration.android.IntentIntegrator
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaCasasScreen(
    casas: List<Casa> = emptyList(),
    navController: NavHostController = rememberNavController(),
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context.applicationContext) }
    val repositoryCasa = remember { RepositoryCasa(NetworkModule.casaApiService) }

    var showEmptyMessage by remember { mutableStateOf(casas.isEmpty()) }

    // Launcher para QR Scanner
    val qrScannerLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult(),
        ) { result ->
            val intentResult = IntentIntegrator.parseActivityResult(result.resultCode, result.data)
            intentResult.contents?.let {
                handleQrResult(
                    it,
                    sessionManager,
                    repositoryCasa,
                    context
                )
            }
                ?: Toast.makeText(context, "Escaneo cancelado", Toast.LENGTH_SHORT).show()
        }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Casas") },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF6200EE), // purple_500
                        titleContentColor = Color.White,
                    ),
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("crear_piso") },
            ) {
                Icon(Icons.Default.Add, "Crear nueva casa")
            }
        },
        bottomBar = {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                Button(
                    onClick = { navController.navigate(Route.Invitaciones.route) },
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Ver Invitaciones")
                }

                Spacer(modifier = Modifier.width(8.dp))

                OutlinedButton(
                    onClick = { iniciarEscanerQr(qrScannerLauncher) },
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Default.QrCode, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Escanear QR")
                }
            }
        },
    ) { padding ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding),
        ) {
            if (showEmptyMessage) {
                Text(
                    text = "Aún no perteneces a ninguna casa.\n¡Crea una nueva!",
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.Center),
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp),
                ) {
                    val currentListas = casas

                    items(
                        count = currentListas.size,
                        key = { index -> currentListas[index].id },
                    ) { index ->
                        val casa = currentListas[index]
                        CasaItem(
                            casa = casa,
                            onClick = {
                                navController.navigate("home?casaId=${casa.id}&casaNombre=${casa.nombre}")
                            },
                        )
                    }
                }
            }
        }
    }
}

// Componente para cada item de casa
@Composable
private fun CasaItem(
    casa: Casa,
    onClick: () -> Unit,
) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = casa.nombre,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            if (!casa.descripcion.isNullOrEmpty()) {
                Text(
                    text = casa.descripcion,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                )
            }
        }
    }
}

// Función para iniciar escáner QR
private fun iniciarEscanerQr(launcher: ActivityResultLauncher<Intent>) {
    val integrator =
        IntentIntegrator.forSupportFragment(null).apply {
            setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
            setPrompt("Escanea el QR del piso")
            setBeepEnabled(true)
        }
    launcher.launch(integrator.createScanIntent())
}

// Función para manejar resultado QR
private fun handleQrResult(
    qrData: String,
    sessionManager: SessionManager,
    repositoryCasa: RepositoryCasa,
    context: Context,
) {
    try {
        val json = JSONObject(qrData)
        if (json.optString("action") == "join_casa") {
            val casaId = json.getLong("casaId")
            val miId = sessionManager.fetchCurrentUserId()

            if (miId == -1L) {
                Toast
                    .makeText(context, "Error: Inicia sesión antes de unirte", Toast.LENGTH_LONG)
                    .show()
                return
            }

            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val token = sessionManager.fetchAuthToken() ?: ""
                    val request = JoinCasaRequest(usuarioId = miId)
                    val response = repositoryCasa.joinCasa(token, casaId, request)

                    if (response.isSuccessful) {
                        Toast.makeText(context, "¡Te has unido al piso!", Toast.LENGTH_LONG).show()
                    } else {
                        Toast
                            .makeText(
                                context,
                                "Error al unirse: ${response.errorBody()?.string()}",
                                Toast.LENGTH_LONG,
                            ).show()
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

// Preview
@Preview
@Composable
fun ListaCasasScreenPreview() {
    ListaCasasScreen(
        casas =
            listOf(
                Casa(1L, "Casa Principal", "Calle Falsa 123", "", ""),
                Casa(2L, "Apartamento Playa", "Avenida del Mar 45", "", ""),
            ),
    )
}
