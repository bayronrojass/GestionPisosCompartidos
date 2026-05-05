package es.mirumi.es.data.repository.repositories

import es.mirumi.es.data.SessionManager
import es.mirumi.es.data.remote.NetworkModule
import es.mirumi.es.data.repository.APIs.EncuestaAPI
import es.mirumi.es.model.requests.EncuestaRequest

class RepositoryEncuesta(
    private val sessionManager: SessionManager,
) {
    private val api = NetworkModule.retrofit.create(EncuestaAPI::class.java)

    // Funciones auxiliares para coger SIEMPRE los datos frescos
    private fun getAuthToken(): String = sessionManager.fetchAuthToken() ?: ""

    private fun getUserId(): Long = sessionManager.fetchCurrentUserId()

    suspend fun obtenerEncuestas(casaId: Long) = api.obtenerEncuestas(getAuthToken(), getUserId(), casaId)

    suspend fun crearEncuesta(
        casaId: Long,
        request: EncuestaRequest,
    ) = api.crearEncuesta(getAuthToken(), getUserId(), casaId, request)

    suspend fun votar(
        encuestaId: Long,
        opcionId: Long,
    ) = api.votar(getAuthToken(), getUserId(), encuestaId, opcionId)

    suspend fun cerrarEncuesta(encuestaId: Long) = api.cerrarEncuesta(getAuthToken(), getUserId(), encuestaId)
}
