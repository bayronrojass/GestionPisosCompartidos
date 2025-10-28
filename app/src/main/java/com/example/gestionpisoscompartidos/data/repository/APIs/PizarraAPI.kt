package com.example.gestionpisoscompartidos.data.repository.APIs

import com.example.gestionpisoscompartidos.model.dtos.PointDeltaDTO
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Streaming

interface PizarraAPI {
    @Streaming
    @GET("lienzos/1/png")
    suspend fun getLienzo(): Response<ResponseBody>

    @GET("lienzos/1/isUpdated")
    suspend fun isUpdated(
        @Query("time") time: Long,
    ): Response<Boolean>

    @POST("lienzos/1/deltas")
    suspend fun postDelta(
        @Body request: List<PointDeltaDTO>,
    ): Response<Boolean>
}
