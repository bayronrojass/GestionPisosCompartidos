package es.mirumi.es.data.repository.APIs

import es.mirumi.es.model.dtos.PostItDTO
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
import retrofit2.http.Query

interface PostItAPI {
    @POST("casas/{id}/{location}/postIt")
    suspend fun crearPostIt(
        @Path("id") id: Long,
        @Path("location") location: String,
        @Query("usuarioId") usuarioId: Long,
    ): Response<PostItDTO>

    @GET("casas/{id}/{location}/postIt")
    suspend fun getPostIts(
        @Path("id") id: Long,
        @Path("location") location: String,
    ): Response<List<PostItDTO>>

    @GET("postits/{id}")
    suspend fun getPostItDetails(
        @Path("id") id: Long,
    ): Response<PostItDTO>

    @DELETE("postits/{id}")
    suspend fun deletePostIt(
        @Path("id") id: Long,
    ): Response<Boolean>

    @POST("postits/pos")
    suspend fun updatePostItPosition(
        @Body postItDTO: PostItDTO,
    ): Response<Boolean>

    // NUEVO ENDPOINT PARA SUBIR EL AUDIO
    @Multipart
    @POST("postits/casa/{casaId}/audio")
    suspend fun crearPostItAudio(
        @Path("casaId") casaId: Long,
        @Part("location") location: RequestBody,
        @Part file: MultipartBody.Part,
    ): Response<PostItDTO>
}
