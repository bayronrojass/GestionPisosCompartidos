package es.mirumi.es.data.repository.repositories

import es.mirumi.es.data.repository.APIs.InvitacionAPI
import es.mirumi.es.model.requests.AccionInvitacionRequest
import es.mirumi.es.model.requests.InvitacionRequest
import es.mirumi.es.model.responses.InvitacionResponse
import retrofit2.Response

class RepositoryInvitacion(
    private val apiService: InvitacionAPI,
) {
    suspend fun crearInvitacion(
        token: String,
        request: InvitacionRequest,
    ): Response<InvitacionResponse> = apiService.crearInvitacion(token, request)

    suspend fun getMisInvitaciones(
        token: String,
        usuarioId: Long,
    ): Response<List<InvitacionResponse>> = apiService.getMisInvitaciones(token, usuarioId)

    suspend fun aceptarInvitacion(
        token: String,
        invitacionId: Long,
        request: AccionInvitacionRequest,
    ): Response<InvitacionResponse> = apiService.aceptarInvitacion(token, invitacionId, request)

    suspend fun rechazarInvitacion(
        token: String,
        invitacionId: Long,
        request: AccionInvitacionRequest,
    ): Response<InvitacionResponse> = apiService.rechazarInvitacion(token, invitacionId, request)
}
