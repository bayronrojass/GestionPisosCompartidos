package com.example.gestionpisoscompartidos.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.gestionpisoscompartidos.data.SessionManager
import com.example.gestionpisoscompartidos.model.Casa
import com.example.gestionpisoscompartidos.ui.invitaciones.InvitacionesScreen
import com.example.gestionpisoscompartidos.ui.item.ItemScreen
import com.example.gestionpisoscompartidos.ui.login.LoginDestination
import com.example.gestionpisoscompartidos.ui.piso.crearPiso.CrearCasaScreen
import com.example.gestionpisoscompartidos.ui.piso.listaPisos.ListaCasasScreen
import kotlinx.serialization.json.Json

@Composable
fun rememberSessionManager(): SessionManager {
    val context = LocalContext.current
    return remember { SessionManager(context.applicationContext) }
}

@Composable
fun AppNavigation(
    sessionManager: SessionManager,
    startDestination: String = Route.Login.route,
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) {
        // LOGIN
        composable(Route.Login.route) {
            LoginDestination(
                navController = navController,
                sessionManager = sessionManager,
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
            InvitacionesScreen(sessionManager = sessionManager)
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
                onNavigateToItem = { listaId, listaNombre ->
                    navController.navigate(
                        Route.Item.createRoute(listaId, listaNombre, casaNombre),
                    )
                },
            )
        }

        // PANTALLA DE ITEMS (ELEMENTOS DE UNA LISTA)
        composable(
            route = Route.Item.route,
            arguments =
                listOf(
                    navArgument("listaId") { type = NavType.LongType },
                    navArgument("listaNombre") { type = NavType.StringType },
                    navArgument("casaNombre") { type = NavType.StringType },
                ),
        ) { backStackEntry ->
            val listaId = backStackEntry.arguments?.getLong("listaId") ?: 0L
            val listaNombre = backStackEntry.arguments?.getString("listaNombre") ?: ""
            val casaNombre = backStackEntry.arguments?.getString("casaNombre") ?: ""

            ItemScreen(
                listaId = listaId,
                listaNombre = listaNombre,
                casaNombre = casaNombre,
            )
        }
    }
}
