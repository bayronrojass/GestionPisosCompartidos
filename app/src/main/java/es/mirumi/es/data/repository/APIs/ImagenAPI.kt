package es.mirumi.es.data.repository.APIs

import es.mirumi.es.model.dtos.ImagenDTO
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface ImagenAPI {
    @Multipart
    @POST("casas/{id}/imagen")
    suspend fun crearImagen(
        @Path("id") id: Long,
        @Part file: MultipartBody.Part,
    ): Response<ImagenDTO>

    @GET("casas/{id}/imagen")
    suspend fun getImagenes(
        @Path("id") id: Long,
    ): Response<List<Long>>

    @GET("imagenes/{id}")
    suspend fun getImagenDetails(
        @Path("id") id: Long,
    ): Response<ImagenDTO>

    @DELETE("imagenes/{id}")
    suspend fun deleteImagen(
        @Path("id") id: Long,
    ): Response<Boolean>

    @POST("imagenes/pos")
    suspend fun updateImagenPosition(
        @Body imagenDTO: ImagenDTO,
    ): Response<Boolean>
}
