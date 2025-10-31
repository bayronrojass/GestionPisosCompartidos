package com.example.gestionpisoscompartidos.data.repository.APIs

import com.example.gestionpisoscompartidos.model.CasaResponse
import com.example.gestionpisoscompartidos.model.Casa // 💡 Asumo que tienes este modelo
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
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

    @GET("casas/{id}")
    suspend fun getCasaDetails(
        @Header("Authorization") token: String,
        @Path("id") casaId: Long
    ): Response<Casa>
}