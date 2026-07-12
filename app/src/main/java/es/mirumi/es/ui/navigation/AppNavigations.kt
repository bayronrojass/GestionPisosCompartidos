package es.mirumi.es.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import es.mirumi.es.data.SessionManager
import es.mirumi.es.model.Casa
import es.mirumi.es.ui.encuestas.EncuestasScreen
import es.mirumi.es.ui.encuestas.EncuestasViewModel
import es.mirumi.es.ui.encuestas.EncuestasViewModelFactory
import es.mirumi.es.ui.home.CalendarioScreen
import es.mirumi.es.ui.home.HomeViewModel
import es.mirumi.es.ui.home.HomeViewModelFactory
import es.mirumi.es.ui.home.PrincipalInicio
import es.mirumi.es.ui.home.PrincipalInicioCarga
import es.mirumi.es.ui.home.PrincipalInicioSesin
import es.mirumi.es.ui.invitaciones.InvitacionesScreen
import es.mirumi.es.ui.item.ItemScreen
import es.mirumi.es.ui.piso.crearPiso.CrearCasaScreen
import es.mirumi.es.ui.piso.gestionUsuarios.GestionUsuariosPiso
import es.mirumi.es.ui.piso.listaPisos.ListaCasasScreen
import es.mirumi.es.ui.utils.LogrosGlobalViewModel
import es.mirumi.es.ui.utils.LogrosGlobalViewModelFactory
import kotlinx.serialization.json.Json
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.util.concurrent.TimeUnit

@Composable
fun rememberSessionManager(): SessionManager {
    val context = LocalContext.current
    return remember { SessionManager(context.applicationContext) }
}

@Composable
fun AppNavigation(
    sessionManager: SessionManager,
    startDestination: String = Route.InicioCarga.route,
) {
    val navController = rememberNavController()

    val logrosGlobalViewModel: LogrosGlobalViewModel =
        viewModel(
            factory = LogrosGlobalViewModelFactory(sessionManager),
        )
    val nuevoLogro by logrosGlobalViewModel.nuevoLogro.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = startDestination,
        ) {
            composable(Route.InicioCarga.route) {
                PrincipalInicioCarga(navController)
            }

            composable(Route.InicioPrincipal.route) {
                PrincipalInicio(navController)
            }

            composable(Route.InicioSesion.route) {
                PrincipalInicioSesin(navController, sessionManager)
            }

            composable(
                route = Route.GestionUsuariosPiso.route,
                arguments = listOf(navArgument("piso_id") { type = NavType.LongType }),
            ) { backStackEntry ->
                val pisoId = backStackEntry.arguments?.getLong("piso_id") ?: 0L
                GestionUsuariosPiso(navController = navController, pisoId = pisoId)
            }

            composable(
                route = Route.ListaCasas.route,
                arguments =
                    listOf(
                        navArgument("casas") {
                            type = NavType.StringType
                            nullable = true
                            defaultValue = null
                        },
                    ),
            ) { backStackEntry ->
                val casasJson = backStackEntry.arguments?.getString("casas")
                val casas =
                    if (!casasJson.isNullOrEmpty()) {
                        Json.decodeFromString<List<Casa>>(casasJson)
                    } else {
                        emptyList()
                    }

                ListaCasasScreen(casas, navController)
            }

            composable(Route.CrearPiso.route) {
                CrearCasaScreen()
            }

            composable(route = Route.Invitaciones.route) {
                val sessionManagerLocal = rememberSessionManager()
                InvitacionesScreen(sessionManager = sessionManagerLocal, navController = navController)
            }

            composable(
                route = Route.Home.route,
                arguments =
                    listOf(
                        navArgument("casaId") { type = NavType.LongType },
                        navArgument("casaNombre") { type = NavType.StringType },
                    ),
            ) { backStackEntry ->
                val casaId = backStackEntry.arguments?.getLong("casaId") ?: 0L
                val casaNombre = backStackEntry.arguments?.getString("casaNombre") ?: ""

                MainScreenWithNavigation(
                    casaId = casaId,
                    casaNombre = casaNombre,
                    navController = navController,
                    onNavigateToItem = { listaId, listaNombre ->
                        navController.navigate(Route.Item.createRoute(listaId, listaNombre, casaNombre, casaId))
                    },
                    onLogout = {
                        navController.navigate(Route.InicioSesion.route) { popUpTo(0) { inclusive = true } }
                    },
                )
            }

            composable(Route.Calendario.route) { backStackEntry ->
                val parentEntry = remember(backStackEntry) { navController.getBackStackEntry(Route.Home.route) }
                val context = LocalContext.current
                val homeViewModel: HomeViewModel =
                    viewModel(
                        parentEntry,
                        factory =
                            HomeViewModelFactory(
                                context = context,
                                sessionManager = sessionManager,
                                casaId =
                                    parentEntry.arguments?.getLong("casaId") ?: 0L,
                            ),
                    )

                CalendarioScreen(onNavigateBack = { navController.popBackStack() }, viewModel = homeViewModel)
            }

            composable(
                route = Route.Item.route,
                arguments =
                    listOf(
                        navArgument("listaId") { type = NavType.LongType },
                        navArgument("listaNombre") { type = NavType.StringType },
                        navArgument("casaNombre") { type = NavType.StringType },
                        navArgument("casaId") { type = NavType.LongType },
                    ),
            ) { backStackEntry ->
                val listaId = backStackEntry.arguments?.getLong("listaId") ?: 0L
                val listaNombre = backStackEntry.arguments?.getString("listaNombre") ?: ""
                val casaNombre = backStackEntry.arguments?.getString("casaNombre") ?: ""
                val casaId = backStackEntry.arguments?.getLong("casaId") ?: 0L

                ItemScreen(
                    navController = navController,
                    listaId = listaId,
                    listaNombre = listaNombre,
                    casaNombre = casaNombre,
                    casaId = casaId,
                )
            }

            composable(
                route = "qr_screen/{casaId}",
                arguments = listOf(navArgument("casaId") { type = NavType.LongType }),
            ) { backStackEntry ->
                val casaId = backStackEntry.arguments?.getLong("casaId") ?: 0L
                val qrViewModel: es.mirumi.es.ui.codigoQR.CodigoQRViewModel =
                    viewModel(
                        factory =
                            object : androidx.lifecycle.ViewModelProvider.Factory {
                                override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T =
                                    es.mirumi.es.ui.codigoQR
                                        .CodigoQRViewModel(sessionManager) as T
                            },
                    )
                es.mirumi.es.ui.codigoQR
                    .QRScreen(casaIdActual = casaId, viewModel = qrViewModel)
            }

            composable(
                route = Route.Ranking.route,
                arguments = listOf(navArgument("casa_id") { type = NavType.LongType }),
            ) { backStackEntry ->
                val casaId = backStackEntry.arguments?.getLong("casa_id") ?: 0L
                es.mirumi.es.ui.ranking
                    .RankingScreen(navController = navController, casaId = casaId)
            }

            composable(
                route = Route.Encuestas.route,
                arguments = listOf(navArgument("casa_id") { type = NavType.LongType }),
            ) { backStackEntry ->
                val casaId = backStackEntry.arguments?.getLong("casa_id") ?: 0L
                val viewModel: EncuestasViewModel = viewModel(factory = EncuestasViewModelFactory(casaId))
                EncuestasScreen(viewModel = viewModel)
            }
        }

        if (nuevoLogro != null) {
            val logro = nuevoLogro!!

            val party =
                Party(
                    speed = 0f,
                    maxSpeed = 30f,
                    damping = 0.9f,
                    spread = 360,
                    colors = listOf(0x8061A2, 0xFFD700, 0x00E676, 0x2196F3), // Morado, Dorado, Verde, Azul
                    emitter = Emitter(duration = 100, TimeUnit.MILLISECONDS).max(100),
                    position = Position.Relative(0.5, 0.3),
                )

            // Lluvia de confeti
            KonfettiView(
                modifier = Modifier.fillMaxSize(),
                parties = listOf(party),
            )

            // Pop-up central
            AlertDialog(
                onDismissRequest = { logrosGlobalViewModel.cerrarPopup() },
                containerColor = Color.White,
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = Color(0xFFFFB300),
                            modifier = Modifier.size(32.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("¡Nuevo Logro Desbloqueado!", fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 20.sp)
                    }
                },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text(text = "Has conseguido la medalla de ${logro.nivel}:", fontSize = 14.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = logro.nombre, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF8061A2))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = logro.descripcion, fontSize = 16.sp, color = Color.DarkGray)
                    }
                },
                confirmButton = {
                    TextButton(onClick = { logrosGlobalViewModel.cerrarPopup() }) {
                        Text("¡Genial!", color = Color(0xFF8061A2), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                },
            )
        }
    }
}
