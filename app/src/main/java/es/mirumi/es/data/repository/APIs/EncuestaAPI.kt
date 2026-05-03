package es.mirumi.es.data.repository.APIs

import es.mirumi.es.model.requests.EncuestaRequest
import es.mirumi.es.model.responses.EncuestaResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface EncuestaAPI {
    @POST("casas/{casaId}/encuestas")
    suspend fun crearEncuesta(
        @Path("casaId") casaId: Long,
        @Body request: EncuestaRequest,
    ): Response<EncuestaResponse>

    @GET("casas/{casaId}/encuestas")
    suspend fun obtenerEncuestas(
        @Path("casaId") casaId: Long,
    ): Response<List<EncuestaResponse>>

    @POST("encuestas/{encuestaId}/votar/{opcionId}")
    suspend fun votar(
        @Path("encuestaId") encuestaId: Long,
        @Path("opcionId") opcionId: Long,
    ): Response<Map<String, String>>

    @PUT("encuestas/{encuestaId}/cerrar")
    suspend fun cerrarEncuesta(
        @Path("encuestaId") encuestaId: Long,
    ): Response<Map<String, String>>
}
