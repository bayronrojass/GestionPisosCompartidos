package com.example.gestionpisoscompartidos.data.repository.APIs

import com.example.gestionpisoscompartidos.model.CasaResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface CasaAPI {
    @Multipart
    @POST("casas")
    suspend fun crearCasa(
        @Part("casa") casa: RequestBody,
        @Part file: MultipartBody.Part?,
    ): Response<CasaResponse>
}
