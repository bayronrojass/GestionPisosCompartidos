package es.mirumi.es.data.repository.repositories

import es.mirumi.es.data.remote.NetworkModule
import es.mirumi.es.data.repository.APIs.EncuestaAPI
import es.mirumi.es.model.requests.EncuestaRequest

class RepositoryEncuesta {
    private val api = NetworkModule.retrofit.create(EncuestaAPI::class.java)

    suspend fun obtenerEncuestas(casaId: Long) = api.obtenerEncuestas(casaId)

    suspend fun crearEncuesta(
        casaId: Long,
        request: EncuestaRequest,
    ) = api.crearEncuesta(casaId, request)

    suspend fun votar(
        encuestaId: Long,
        opcionId: Long,
    ) = api.votar(encuestaId, opcionId)

    suspend fun cerrarEncuesta(encuestaId: Long) = api.cerrarEncuesta(encuestaId)
}
