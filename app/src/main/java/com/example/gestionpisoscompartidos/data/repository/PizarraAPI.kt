package com.example.gestionpisoscompartidos.data.repository

import com.example.gestionpisoscompartidos.model.dtos.PointDeltaDTO
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface PizarraAPI {
    @GET("lienzo/1")
    suspend fun getLienzo(): Response<ByteArray>

    @POST("lienzo/1/delta")
    suspend fun postDelta(
        @Body request: List<PointDeltaDTO>,
    ): Response<Boolean>
}
