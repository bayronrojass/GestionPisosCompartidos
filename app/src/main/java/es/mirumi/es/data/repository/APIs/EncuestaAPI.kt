package es.mirumi.es.data.repository.APIs

import es.mirumi.es.model.requests.EncuestaRequest
import es.mirumi.es.model.responses.EncuestaResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface EncuestaAPI {
    @POST("api/casa/{casaId}/lista-encuestas")
    suspend fun crearEncuesta(
        @Header("Authorization") token: String, // <- AÑADIDO
        @Path("casaId") casaId: Long,
        @Body request: EncuestaRequest,
    ): Response<EncuestaResponse>

    @GET("api/casa/{casaId}/lista-encuestas")
    suspend fun obtenerEncuestas(
        @Header("Authorization") token: String, // <- AÑADIDO
        @Path("casaId") casaId: Long,
    ): Response<List<EncuestaResponse>>

    @POST("api/encuestas/{encuestaId}/votar-opcion/{opcionId}")
    suspend fun votar(
        @Header("Authorization") token: String, // <- AÑADIDO
        @Path("encuestaId") encuestaId: Long,
        @Path("opcionId") opcionId: Long,
    ): Response<Map<String, String>>

    @PUT("api/encuestas/{encuestaId}/cerrar-encuesta")
    suspend fun cerrarEncuesta(
        @Header("Authorization") token: String, // <- AÑADIDO
        @Path("encuestaId") encuestaId: Long,
    ): Response<Map<String, String>>
}
