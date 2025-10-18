package com.example.gestionpisoscompartidos.data

import com.example.gestionpisoscompartidos.model.LoginRequest
import com.example.gestionpisoscompartidos.model.LoginResponse
import com.example.gestionpisoscompartidos.model.Usuario
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface DatabaseAPI{
    @POST("login")
    suspend fun login(@Body credenciales: LoginRequest): Response<LoginResponse>
}
