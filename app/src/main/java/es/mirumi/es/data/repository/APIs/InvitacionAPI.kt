package es.mirumi.es.data.repository.APIs

import es.mirumi.es.model.requests.AccionInvitacionRequest
import es.mirumi.es.model.requests.InvitacionRequest
import es.mirumi.es.model.responses.InvitacionResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface InvitacionAPI {
    @POST("invitaciones")
    suspend fun crearInvitacion(
        @Header("Authorization") token: String,
        @Body request: InvitacionRequest,
    ): Response<InvitacionResponse>

    @GET("invitaciones/{usuarioId}")
    suspend fun getMisInvitaciones(
        @Header("Authorization") token: String,
        @Path("usuarioId") usuarioId: Long,
    ): Response<List<InvitacionResponse>>

    @POST("invitaciones/{id}/aceptar")
    suspend fun aceptarInvitacion(
        @Header("Authorization") token: String,
        @Path("id") invitacionId: Long,
        @Body request: AccionInvitacionRequest,
    ): Response<InvitacionResponse>

    @POST("invitaciones/{id}/rechazar")
    suspend fun rechazarInvitacion(
        @Header("Authorization") token: String,
        @Path("id") invitacionId: Long,
        @Body request: AccionInvitacionRequest,
    ): Response<InvitacionResponse>
}
