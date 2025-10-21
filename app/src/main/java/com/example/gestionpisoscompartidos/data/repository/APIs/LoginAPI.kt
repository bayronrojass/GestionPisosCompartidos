package com.example.gestionpisoscompartidos.data.repository.APIs

import com.example.gestionpisoscompartidos.model.LoginRequest
import com.example.gestionpisoscompartidos.model.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface LoginAPI {
    @POST("login")
    suspend fun login(
        @Body credenciales: LoginRequest,
    ): Response<LoginResponse>

}
