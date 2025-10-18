package com.example.gestionpisoscompartidos.services
import retrofit2.Call
import retrofit2.http.GET

interface TestApiService {
    @GET("api/hello")
    fun getHello(): Call<String>
}
