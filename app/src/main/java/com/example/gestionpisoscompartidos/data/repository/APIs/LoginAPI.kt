package com.example.gestionpisoscompartidos.data.repository.APIs

import com.example.gestionpisoscompartidos.model.requests.LoginRequest
import com.example.gestionpisoscompartidos.model.responses.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface LoginAPI {
    @POST("login")
    suspend fun login(
        @Body credenciales: LoginRequest,
    ): Response<LoginResponse>
}
