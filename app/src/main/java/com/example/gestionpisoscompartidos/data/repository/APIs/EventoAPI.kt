package com.example.gestionpisoscompartidos.data.repository.APIs

import com.example.gestionpisoscompartidos.model.eventRequest
import com.example.gestionpisoscompartidos.model.eventoResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST
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
}
