package es.mirumi.es.data.repository.repositories

import es.mirumi.es.data.SessionManager
import es.mirumi.es.data.remote.NetworkModule
import es.mirumi.es.data.repository.APIs.EncuestaAPI
import es.mirumi.es.model.requests.EncuestaRequest

class RepositoryEncuesta(
    private val sessionManager: SessionManager,
) {
    private val api = NetworkModule.retrofit.create(EncuestaAPI::class.java)

    // Esta función saca el token fresco de las preferencias CADA VEZ que se llama
    private fun getAuthToken(): String {
        val token = sessionManager.fetchAuthToken()
        if (token.isNullOrEmpty()) {
            println("⚠️ ALERTA: Intentando hacer petición de encuesta sin Token guardado.")
            return ""
        }
        return token
    }

    suspend fun obtenerEncuestas(casaId: Long) = api.obtenerEncuestas(getAuthToken(), casaId)

    suspend fun crearEncuesta(
        casaId: Long,
        request: EncuestaRequest,
    ) = api.crearEncuesta(getAuthToken(), casaId, request)

    suspend fun votar(
        encuestaId: Long,
        opcionId: Long,
    ) = api.votar(getAuthToken(), encuestaId, opcionId)

    suspend fun cerrarEncuesta(encuestaId: Long) = api.cerrarEncuesta(getAuthToken(), encuestaId)
}
