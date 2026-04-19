package es.mirumi.es.data.repository.APIs

import es.mirumi.es.model.Evento
import es.mirumi.es.model.Gasto
import es.mirumi.es.model.Usuario
import es.mirumi.es.model.requests.GastoRequest
import es.mirumi.es.model.requests.JoinCasaRequest
import es.mirumi.es.model.responses.CasaDetailsResponseDTO
import es.mirumi.es.model.responses.CasaResponse
import es.mirumi.es.model.Casa
import es.mirumi.es.model.dtos.BorradorGastoDTO
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

interface CasaAPI {
    @Multipart
    @POST("casas")
    suspend fun crearCasa(
        @Part("casa") casa: RequestBody,
        @Part file: MultipartBody.Part?,
    ): Response<CasaResponse>

    @GET("casas/{id}/lienzo")
    suspend fun getLienzo(
        @Path("id") id: Long,
    ): Response<Long>

    @GET("casas/{id}/details")
    suspend fun getPisoDetails(
        @Header("Authorization") token: String,
        @Path("id") pisoId: Long,
    ): Response<CasaDetailsResponseDTO>

    @DELETE("casas/{casaId}/miembros/{usuarioId}")
    suspend fun removeMiembro(
        @Header("Authorization") token: String,
        @Path("casaId") casaId: Long,
        @Path("usuarioId") usuarioId: Long,
    ): Response<Unit>

    @GET("casas/{id}/miembros")
    suspend fun getPisoMiembros(
        @Header("Authorization") token: String,
        @Path("id") pisoId: Long,
    ): Response<List<Usuario>>

    @POST("casas/{casaId}/join")
    suspend fun joinCasa(
        @Header("Authorization") token: String,
        @Path("casaId") casaId: Long,
        @Body request: JoinCasaRequest,
    ): Response<String>

    @GET("casas/{id}/eventos")
    suspend fun getEventosCasa(
        @Header("Authorization") token: String,
        @Path("id") casaId: Long,
    ): Response<List<Evento>>

    @GET("casas/{id}/gastos")
    suspend fun getGastosCasa(
        @Header("Authorization") token: String,
        @Path("id") id: Long,
    ): Response<List<Gasto>>

    @POST("casas/{casaId}/crearGasto")
    suspend fun postGastoCasa(
        @Header("Authorization") token: String,
        @Path("casaId") casaId: Long,
        @Body request: GastoRequest,
    ): Response<ResponseBody>

    @Multipart
    @POST("casas/escanear-ticket")
    suspend fun escanearTicket(
        @Header("Authorization") token: String,
        @Part file: MultipartBody.Part,
    ): Response<BorradorGastoDTO>

    @PUT("casas/{casaId}/gastos/{gastoId}")
    suspend fun editarGasto(
        @Header("Authorization") token: String,
        @Path("casaId") casaId: Long,
        @Path("gastoId") gastoId: Long,
        @Body request: GastoRequest,
    ): Response<Unit>

    @GET("casas/usuario/{usuarioId}")
    suspend fun getMisCasas(
        @Header("Authorization") token: String,
        @Path("usuarioId") usuarioId: Long,
    ): Response<List<Casa>>
}
