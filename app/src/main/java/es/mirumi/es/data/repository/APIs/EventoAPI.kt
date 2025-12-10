package es.mirumi.es.data.repository.APIs

import es.mirumi.es.model.Elemento
import es.mirumi.es.model.requests.eventRequest
import es.mirumi.es.model.responses.eventoResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

// EventoAPI.kt
interface EventoAPI {
    @POST("casas/{casaId}/eventos")
    suspend fun crearEvento(
        @Path("casaId") casaId: Long,
        @Body eventoRequest: eventRequest,
    ): Response<eventoResponse>

    @DELETE("casas/eventos/{eventoId}")
    suspend fun eliminarEvento(
        @Path("eventoId") eventoId: Long,
    ): Response<Unit>

    @PUT("casas/eventos/{eventoId}")
    suspend fun actualizarEvento(
        @Path("eventoId") eventoId: Long,
        @Body elemento: eventRequest,
    ): Response<Elemento>
}
