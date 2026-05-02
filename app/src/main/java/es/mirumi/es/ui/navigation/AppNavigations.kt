package es.mirumi.es.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import es.mirumi.es.data.SessionManager
import es.mirumi.es.model.Casa
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
import kotlinx.serialization.json.Json

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

        // GESTIÓN DE USUARIOS
        composable(
            route = Route.GestionUsuariosPiso.route,
            arguments =
                listOf(
                    navArgument("piso_id") { type = NavType.LongType },
                ),
        ) { backStackEntry ->
            val pisoId = backStackEntry.arguments?.getLong("piso_id") ?: 0L

            GestionUsuariosPiso(
                navController = navController,
                pisoId = pisoId,
            )
        }

        // LISTA DE PISOS
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

        // CREAR PISO
        composable(Route.CrearPiso.route) {
            CrearCasaScreen()
        }

        // INVITACIONES:
        composable(route = Route.Invitaciones.route) {
            val sessionManager = rememberSessionManager()
            InvitacionesScreen(
                sessionManager = sessionManager,
                navController = navController,
            )
        }

        // HOME/DASHBOARD DE CASA
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
                    navController.navigate(
                        Route.Item.createRoute(listaId, listaNombre, casaNombre, casaId),
                    )
                },
                onLogout = {
                    navController.navigate(Route.InicioSesion.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
            )
        }

        composable(Route.Calendario.route) { backStackEntry ->

            val parentEntry =
                remember(backStackEntry) {
                    navController.getBackStackEntry(Route.Home.route)
                }

            val context = LocalContext.current

            val homeViewModel: HomeViewModel =
                viewModel(
                    parentEntry,
                    factory =
                        HomeViewModelFactory(
                            context = context,
                            sessionManager = sessionManager,
                            casaId = parentEntry.arguments?.getLong("casaId") ?: 0L,
                        ),
                )

            CalendarioScreen(
                onNavigateBack = { navController.popBackStack() },
                viewModel = homeViewModel,
            )
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
            arguments = listOf(androidx.navigation.navArgument("casaId") { type = androidx.navigation.NavType.LongType }),
        ) { backStackEntry ->
            val casaId = backStackEntry.arguments?.getLong("casaId") ?: 0L

            val qrViewModel: es.mirumi.es.ui.codigoQR.CodigoQRViewModel =
                androidx.lifecycle.viewmodel.compose.viewModel(
                    factory =
                        object : androidx.lifecycle.ViewModelProvider.Factory {
                            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T =
                                es.mirumi.es.ui.codigoQR
                                    .CodigoQRViewModel(sessionManager) as T
                        },
                )

            es.mirumi.es.ui.codigoQR.QRScreen(
                casaIdActual = casaId,
                viewModel = qrViewModel,
            )
        }

        // RANKING DE CASA
        composable(
            route = Route.Ranking.route,
            arguments =
                listOf(
                    navArgument("casa_id") { type = NavType.LongType },
                ),
        ) { backStackEntry ->
            val casaId = backStackEntry.arguments?.getLong("casa_id") ?: 0L

            es.mirumi.es.ui.ranking.RankingScreen(
                navController = navController,
                casaId = casaId,
            )
        }
    }
}
