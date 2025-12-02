package com.example.gestionpisoscompartidos.ui.invitaciones

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gestionpisoscompartidos.data.SessionManager
import com.example.gestionpisoscompartidos.data.remote.NetworkModule
import com.example.gestionpisoscompartidos.data.repository.repositories.RepositoryInvitacion
import com.example.gestionpisoscompartidos.model.responses.InvitacionResponse
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvitacionesScreen(sessionManager: SessionManager) {
    val viewModel: InvitacionesViewModel =
        viewModel(
            factory = InvitacionesViewModelFactory(RepositoryInvitacion(NetworkModule.invitacionApiService), sessionManager),
        )

    val invitaciones by viewModel.invitaciones.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Cargar invitaciones al iniciar
    LaunchedEffect(Unit) {
        viewModel.fetchMisInvitaciones()
    }

    // Observar errores
    LaunchedEffect(error) {
        error?.let { errorMsg ->
            Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Invitaciones") },
//                backgroundColor = Color.White,
//                contentColor = Color.Black,
//                elevation = 4.dp
            )
        },
    ) { innerPadding ->
        Box(
            modifier =
                Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
        ) {
            when {
                isLoading -> {
                    // Estado de carga
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(16.dp))
                        Text("Cargando invitaciones...")
                    }
                }
                invitaciones.isEmpty() -> {
                    // Estado vacío
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            "No tienes invitaciones pendientes",
                            fontSize = 18.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp),
                        )
                    }
                }
                else -> {
                    // Lista de invitaciones
                    LazyColumn(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                    ) {
                        item {
                            Text(
                                "Mis Invitaciones",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 16.dp),
                            )
                        }

                        val currentListas = invitaciones ?: emptyList()

                        items(
                            count = currentListas.size,
                            key = { index -> currentListas[index].id }, // Usar ID como key única
                        ) { index ->
                            val invitacion = currentListas[index]
                            InvitacionItem(
                                invitacion = invitacion,
                                onAcceptClick = {
                                    scope.launch {
                                        viewModel.aceptarInvitacion(invitacion.id)
                                    }
                                },
                                onRejectClick = {
                                    scope.launch {
                                        viewModel.rechazarInvitacion(invitacion.id)
                                    }
                                },
                            )
                            Spacer(Modifier.height(8.dp))
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
    onAcceptClick: () -> Unit,
    onRejectClick: () -> Unit,
) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
        //        elevation = 4.dp,
//        backgroundColor = Color.White
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
        ) {
            // Información de la invitación
            Text(
                text = invitacion.casaNombre ?: "Invitación a piso",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
            )

            Spacer(Modifier.height(8.dp))

            // Botones de acción
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                Button(
                    onClick = onRejectClick,
//                    colors = ButtonDefaults.buttonColors(
//                        backgroundColor = Color.LightGray
//                    ),
                    modifier = Modifier.padding(end = 8.dp),
                ) {
                    Text("Rechazar", color = Color.Black)
                }

                Button(
                    onClick = onAcceptClick,
//                    colors = ButtonDefaults.buttonColors(
//                        backgroundColor = Color(0xFF6A5ACD)
//                    )
                ) {
                    Text("Aceptar", color = Color.White)
                }
            }
        }
    }
}
