package com.example.gestionpisoscompartidos.data.repository.APIs

import com.example.gestionpisoscompartidos.model.CasaResponse
import com.example.gestionpisoscompartidos.model.dtos.PostItDTO
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
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

    @POST("casas/{id}/postIt")
    suspend fun crearPostIt(
        @Path("id") id: Long,
    ): Response<PostItDTO>

    @GET("casas/{id}/lienzo")
    suspend fun getLienzo(
        @Path("id") id: Long,
    ): Response<Long>

    @GET("casas/{id}/postIt")
    suspend fun getPostIts(
        @Path("id") id: Long,
    ): Response<List<Long>>

    @GET("postits/{id}")
    suspend fun getPostItDetails(
        @Path("id") id: Long,
    ): Response<PostItDTO>

    @DELETE("postits/{id}")
    suspend fun deletePostIt(
        @Path("id") id: Long,
    ): Response<Boolean>

    @POST("postits/{id}/pos")
    suspend fun updatePostItPosition(
        @Path("id") id: Long,
        @Body postItDTO: PostItDTO,
    ): Response<Boolean>
}
