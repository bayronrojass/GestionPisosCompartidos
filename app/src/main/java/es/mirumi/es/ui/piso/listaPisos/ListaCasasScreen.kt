package es.mirumi.es.ui.piso.listaPisos

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
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

@Composable
fun ListaCasasScreen(
    casas: List<Casa> = emptyList(),
    navController: NavHostController,
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context.applicationContext) }
    val repositoryCasa = remember { RepositoryCasa(NetworkModule.casaApiService) }

    val viewModel: ListaCasasViewModel =
        viewModel(
            factory = ListaCasasViewModelFactory(repositoryCasa, sessionManager),
        )
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.cargarCasas(casas)
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    val qrLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val scanResult = IntentIntegrator.parseActivityResult(result.resultCode, result.data)
            if (scanResult.contents != null) {
                handleQrAction(scanResult.contents, viewModel, sessionManager, repositoryCasa, context)
            }
        }

    Scaffold(
        containerColor = Color(0xFFF8F8F8),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Route.CrearPiso.route) },
                containerColor = Color.Black,
                contentColor = Color.White,
                shape = CircleShape,
            ) { Icon(Icons.Default.Add, "Nuevo Piso") }
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp)) {
            HeaderSection()

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                QuickActionButton("Invitaciones", R.drawable.ic_email, Modifier.weight(1f)) {
                    navController.navigate(Route.Invitaciones.route)
                }
                QuickActionButton("Escanear QR", Icons.Default.QrCodeScanner, Modifier.weight(1f)) {
                    val integrator =
                        IntentIntegrator(context as Activity).apply {
                            setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
                            setPrompt("Enfoca el QR del piso")
                            setBeepEnabled(true)
                            setOrientationLocked(false)
                        }
                    qrLauncher.launch(integrator.createScanIntent())
                }
            }

            Spacer(Modifier.height(24.dp))

            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF8061A2))
                }
            } else if (uiState.casas.isEmpty()) {
                EmptyState()
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(bottom = 80.dp)) {
                    items(uiState.casas, key = { it.id }) { casa ->
                        CasaCard(casa) {
                            sessionManager.saveCasaActiva(casa.id, casa.nombre)
                            navController.navigate(Route.Home.createRoute(casa.id, casa.nombre))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HeaderSection() {
    Spacer(Modifier.height(24.dp))
    Text("Mis pisos", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.Black)
    Spacer(Modifier.height(20.dp))
}

@Composable
fun QuickActionButton(
    label: String,
    icon: Any,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    Surface(
        modifier = modifier.height(56.dp).clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 2.dp,
    ) {
        Row(
            Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            when (icon) {
                is Int -> Icon(painterResource(icon), null, tint = Color(0xFF8061A2), modifier = Modifier.size(20.dp))
                is androidx.compose.ui.graphics.vector.ImageVector -> Icon(icon, null, tint = Color(0xFF8061A2))
            }
            Spacer(Modifier.width(8.dp))
            Text(label, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun CasaCard(
    casa: Casa,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp),
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(48.dp).clip(CircleShape).background(Color(0xFFF0EBF5)), contentAlignment = Alignment.Center) {
                Icon(painterResource(R.drawable.iconopiso), null, tint = Color(0xFF8061A2), modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(casa.nombre, fontWeight = FontWeight.Bold, fontSize = 18.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    casa.descripcion ?: "Sin descripción",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Icon(
                painterResource(R.drawable.vector19),
                null,
                tint = Color(0xFF8061A2),
                modifier = Modifier.size(16.dp).graphicsLayer(rotationZ = 180f),
            )
        }
    }
}

@Composable
fun EmptyState() {
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Image(
            painterResource(R.drawable.casita),
            null,
            Modifier.size(100.dp).graphicsLayer(alpha = 0.2f),
            colorFilter = ColorFilter.tint(Color.Gray),
        )
        Spacer(Modifier.height(16.dp))
        Text("No tienes pisos todavía.\n¡Crea uno o únete con un QR!", textAlign = TextAlign.Center, color = Color.Gray)
    }
}

private fun handleQrAction(
    data: String,
    vm: ListaCasasViewModel,
    sm: SessionManager,
    repo: RepositoryCasa,
    ctx: Context,
) {
    try {
        // URI mirumi://invite?casaId=X
        val casaIdLeido: Long =
            try {
                val uri = android.net.Uri.parse(data)
                val idStr = uri.getQueryParameter("casaId")
                idStr?.toLongOrNull() ?: throw Exception("Not a URI")
            } catch (e: Exception) {
                try {
                    val json = JSONObject(data)
                    if (json.has("casaId")) json.getLong("casaId") else json.getLong("id")
                } catch (e: Exception) {
                    data.trim().toLong()
                }
            }

        val usuarioId = sm.fetchCurrentUserId()
        if (usuarioId == -1L) {
            Toast.makeText(ctx, "Error: Sesión no válida", Toast.LENGTH_SHORT).show()
            return
        }

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val token = sm.fetchAuthToken() ?: ""
                val response = repo.joinCasa(token, casaIdLeido, JoinCasaRequest(usuarioId))

                if (response.isSuccessful) {
                    Toast.makeText(ctx, "¡Te has unido con éxito!", Toast.LENGTH_LONG).show()
                    vm.refreshCasas()
                } else {
                    Toast.makeText(ctx, "Error al unirse: Posiblemente ya eres miembro.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(ctx, "Error de red al unirse a la casa", Toast.LENGTH_SHORT).show()
            }
        }
    } catch (e: Exception) {
        Toast.makeText(ctx, "El código QR no pertenece a Mirumi", Toast.LENGTH_SHORT).show()
    }
}
