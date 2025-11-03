package com.example.gestionpisoscompartidos.data.repository.repositories

import com.example.gestionpisoscompartidos.data.repository.APIs.InvitacionAPI
import com.example.gestionpisoscompartidos.model.InvitacionRequest
import com.example.gestionpisoscompartidos.model.InvitacionResponse
import retrofit2.Response

class RepositoryInvitacion(
    private val apiService: InvitacionAPI,
) {
    suspend fun crearInvitacion(
        token: String,
        request: InvitacionRequest,
    ): Response<InvitacionResponse> = apiService.crearInvitacion(token, request)

    suspend fun getMisInvitaciones(token: String): Response<List<InvitacionResponse>> = apiService.getMisInvitaciones(token)

    suspend fun aceptarInvitacion(
        token: String,
        invitacionId: Long,
    ): Response<InvitacionResponse> = apiService.aceptarInvitacion(token, invitacionId)

    suspend fun rechazarInvitacion(
        token: String,
        invitacionId: Long,
    ): Response<InvitacionResponse> = apiService.rechazarInvitacion(token, invitacionId)
}
