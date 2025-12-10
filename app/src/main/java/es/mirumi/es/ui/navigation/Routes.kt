package es.mirumi.es.ui.navigation

import java.net.URLEncoder

sealed class Route(
    val route: String,
) {
    // Autenticación
    object Login : Route("login")

    // Pantallas Principales
    object ListaCasas : Route("lista_casas?casas={casas}") {
        fun createRoute(casas: String = "") = "lista_casas?casas=$casas"
    }

    object CrearPiso : Route("crear_piso")

    object Home : Route("home?casaId={casaId}&casaNombre={casaNombre}") {
        fun createRoute(
            casaId: Long,
            casaNombre: String,
        ) = "home?casaId=$casaId&casaNombre=${URLEncoder.encode(casaNombre, "UTF-8")}"
    }

    // Funcionalidades
    object ListaDeListas : Route("listas?casaId={casaId}&casaNombre={casaNombre}") {
        fun createRoute(
            casaId: Long,
            casaNombre: String,
        ) = "listas?casaId=$casaId&casaNombre=${URLEncoder.encode(casaNombre, "UTF-8")}"
    }

    object Item : Route("item?listaId={listaId}&listaNombre={listaNombre}&casaNombre={casaNombre}") {
        fun createRoute(
            listaId: Long,
            listaNombre: String,
            casaNombre: String,
        ) = "item?listaId=$listaId&listaNombre=${URLEncoder.encode(
            listaNombre,
            "UTF-8",
        )}&casaNombre=${URLEncoder.encode(casaNombre, "UTF-8")}"
    }

    object Tareas : Route("tareas?casaId={casaId}&casaNombre={casaNombre}") {
        fun createRoute(
            casaId: Long,
            casaNombre: String,
        ) = "tareas?casaId=$casaId&casaNombre=${URLEncoder.encode(casaNombre, "UTF-8")}"
    }

    object Pizarra : Route("pizarra?casa_id={casa_id}") {
        fun createRoute(casaId: Long) = "pizarra?casa_id=$casaId"
    }

    object GestionUsuariosPiso : Route("gestion_usuarios?piso_id={piso_id}") {
        fun createRoute(pisoId: Long) = "gestion_usuarios?piso_id=$pisoId"
    }

    object Invitaciones : Route("invitaciones")

    object Estadisticas : Route("estadisticas/{casaId}") {
        fun createRoute(casaId: Long) = "estadisticas/$casaId"
    }

    object InicioCarga : Route("inicio_carga")

    object InicioPrincipal : Route("inicio_principal")

    object InicioSesion : Route("inicio_sesion")

    object Calendario : Route("calendario")
}
