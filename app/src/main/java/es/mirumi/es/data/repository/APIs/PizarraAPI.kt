package es.mirumi.es.data.repository.APIs

import es.mirumi.es.model.dtos.PointDeltaDTO
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming

interface PizarraAPI {
    @Streaming
    @GET("lienzos/{id}/imagen")
    suspend fun getLienzo(
        @Path("id") id: Long,
    ): Response<ResponseBody>

    @GET("lienzos/{id}/isUpdated")
    suspend fun isUpdated(
        @Path("id") id: Long,
        @Query("time") time: Long,
    ): Response<Boolean>

    @POST("lienzos/{id}/deltas")
    suspend fun postDelta(
        @Path("id") id: Long,
        @Body request: List<PointDeltaDTO>,
    ): Response<Boolean>

    /**
     * Server-side "Borrar" — wipes the composited bitmap to a blank white surface.
     * Called from `PizarraViewModel.clearOnServer` when the user taps the Borrar button.
     * Without this the server keeps compositing new strokes onto the pre-clear bitmap,
     * so ghost strokes resurrect on the next stroke's poll or on reopen.
     */
    @PUT("lienzos/{id}/clear")
    suspend fun clearLienzo(
        @Path("id") id: Long,
    ): Response<Boolean>
}
