package com.example.gestionpisoscompartidos.data.repository.APIs

import com.example.gestionpisoscompartidos.model.CasaDetailsResponse // <-- 1. Importar el DTO nuevo
import com.example.gestionpisoscompartidos.model.CasaResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.* // <-- Importa DELETE

interface CasaAPI {
    @Multipart
    @POST("casas")
    suspend fun crearCasa(
        @Part("casa") casa: RequestBody,
        @Part file: MultipartBody.Part?,
    ): Response<CasaResponse>

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
}
