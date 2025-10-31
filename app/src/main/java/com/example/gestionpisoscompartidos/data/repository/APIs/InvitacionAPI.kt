package com.example.gestionpisoscompartidos.data.repository.APIs

import com.example.gestionpisoscompartidos.model.InvitacionRequest
import com.example.gestionpisoscompartidos.model.InvitacionResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Header
import retrofit2.http.Path

interface InvitacionAPI {
    @POST("invitaciones")
    suspend fun crearInvitacion(
        @Header("Authorization") token: String,
        @Body request: InvitacionRequest,
    ): Response<InvitacionResponse>

    @GET("invitaciones/me")
    suspend fun getMisInvitaciones(
        @Header("Authorization") token: String,
    ): Response<List<InvitacionResponse>>

    @POST("invitaciones/{id}/aceptar")
    suspend fun aceptarInvitacion(
        @Header("Authorization") token: String,
        @Path("id") invitacionId: Long,
    ): Response<InvitacionResponse>

    @POST("invitaciones/{id}/rechazar")
    suspend fun rechazarInvitacion(
        @Header("Authorization") token: String,
        @Path("id") invitacionId: Long,
    ): Response<InvitacionResponse>
}
