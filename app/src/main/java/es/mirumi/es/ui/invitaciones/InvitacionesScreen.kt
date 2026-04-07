package es.mirumi.es.ui.invitaciones

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import es.mirumi.es.data.SessionManager
import es.mirumi.es.data.remote.NetworkModule
import es.mirumi.es.data.repository.repositories.RepositoryInvitacion
import es.mirumi.es.model.responses.InvitacionResponse
import es.mirumi.es.ui.navigation.Route
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvitacionesScreen(
    sessionManager: SessionManager,
    navController: NavController,
) {
    val viewModel: InvitacionesViewModel =
        viewModel(
            factory =
                InvitacionesViewModelFactory(
                    RepositoryInvitacion(NetworkModule.invitacionApiService),
                    sessionManager,
                ),
        )

    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.fetchMisInvitaciones()
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is InvitacionEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                is InvitacionEvent.NavigateToCasa -> {
                    Toast.makeText(context, "¡Bienvenido a ${event.casaNombre}!", Toast.LENGTH_SHORT).show()

                    navController.navigate(Route.Home.createRoute(event.casaId, event.casaNombre)) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            scope.launch { snackbarHostState.showSnackbar(it) }
            viewModel.errorShown()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(title = { Text("Mis Invitaciones", fontWeight = FontWeight.Bold) })
        },
    ) { innerPadding ->
        Box(
            modifier =
                Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .padding(16.dp),
            contentAlignment = Alignment.Center,
        ) {
            when {
                uiState.isLoading && uiState.invitaciones.isEmpty() -> {
                    CircularProgressIndicator()
                }

                uiState.invitaciones.isEmpty() -> {
                    Text(
                        text = "No tienes invitaciones pendientes",
                        fontSize = 18.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                    )
                }

                else -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(
                            items = uiState.invitaciones,
                            key = { it.id },
                        ) { invitacion ->
                            InvitacionItem(
                                invitacion = invitacion,
                                isProcessing = uiState.isLoading,
                                onAcceptClick = { viewModel.aceptarInvitacion(invitacion) },
                                onRejectClick = { viewModel.rechazarInvitacion(invitacion) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InvitacionItem(
    invitacion: InvitacionResponse,
    isProcessing: Boolean,
    onAcceptClick: () -> Unit,
    onRejectClick: () -> Unit,
) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
        ) {
            Text(
                text = "🏠 ${invitacion.casaNombre}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
            )

            Text(
                text = "Invitado por: ${invitacion.remitenteNombre}",
                fontSize = 14.sp,
                color = Color.DarkGray,
                modifier = Modifier.padding(top = 4.dp),
            )

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(
                    onClick = onRejectClick,
                    enabled = !isProcessing,
                    modifier = Modifier.padding(end = 8.dp),
                ) {
                    Text("Rechazar", color = if (isProcessing) Color.Gray else Color.Red)
                }

                Button(
                    onClick = onAcceptClick,
                    enabled = !isProcessing,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xff8061a2)),
                ) {
                    Text("Aceptar", color = Color.White)
                }
            }
        }
    }
}
