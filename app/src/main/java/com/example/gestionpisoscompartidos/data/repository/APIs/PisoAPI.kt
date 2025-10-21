package com.example.gestionpisoscompartidos.data.repository.APIs

import com.example.gestionpisoscompartidos.model.PisoRequest
import com.example.gestionpisoscompartidos.model.PisoResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface PisoAPI {
    @POST("pisos")
    suspend fun crearPiso(
        @Body nuevoPiso: PisoRequest,
    ): Response<PisoResponse>
}
