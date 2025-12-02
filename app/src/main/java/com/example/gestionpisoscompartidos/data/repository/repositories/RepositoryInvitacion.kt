package com.example.gestionpisoscompartidos.data.repository.repositories

import com.example.gestionpisoscompartidos.data.repository.APIs.InvitacionAPI
import com.example.gestionpisoscompartidos.model.requests.AccionInvitacionRequest
import com.example.gestionpisoscompartidos.model.requests.InvitacionRequest
import com.example.gestionpisoscompartidos.model.responses.InvitacionResponse
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
