package com.example.gestionpisoscompartidos.data.repository.APIs

import com.example.gestionpisoscompartidos.model.CasaDetailsResponse
import com.example.gestionpisoscompartidos.model.CasaResponse
import com.example.gestionpisoscompartidos.model.JoinCasaRequest
import com.example.gestionpisoscompartidos.model.Usuario
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface CasaAPI {
    @Multipart
    @POST("casas")
    suspend fun crearCasa(
        @Part("casa") casa: RequestBody,
        @Part file: MultipartBody.Part?,
    ): Response<CasaResponse>

    @GET("casas/{id}/lienzo")
    suspend fun getLienzo(
        @Path("id") id: Long,
    ): Response<Long>

    @GET("casas/{id}/details")
    suspend fun getPisoDetails(
        @Header("Authorization") token: String,
        @Path("id") pisoId: Long,
    ): Response<CasaDetailsResponse> // Devuelve el DTO con los miembros

    @DELETE("casas/{casaId}/miembros/{usuarioId}")
    suspend fun removeMiembro(
        @Header("Authorization") token: String,
        @Path("casaId") casaId: Long,
        @Path("usuarioId") usuarioId: Long,
    ): Response<Unit>

    @GET("casas/{id}/miembros")
    suspend fun getPisoMiembros(
        @Header("Authorization") token: String,
        @Path("id") pisoId: Long,
    ): Response<List<Usuario>>

    @POST("casas/{casaId}/join")
    suspend fun joinCasa(
        @Header("Authorization") token: String,
        @Path("casaId") casaId: Long,
        @Body request: JoinCasaRequest,
    ): Response<String>
}
