package es.mirumi.es.data.repository.APIs

import es.mirumi.es.model.Tarea
import es.mirumi.es.model.requests.TareaRequest
import es.mirumi.es.model.responses.PaginatedResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface TareaAPI {
    @GET("casas/{casaId}/tareas")
    suspend fun getTareasByCasaId(
        @Path("casaId") casaId: Long,
        @Query("completado") completado: Boolean? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 100,
        @Query("sort") sort: String? = null,
    ): Response<PaginatedResponse<Tarea>>

    @POST("casas/{casaId}/tareas")
    suspend fun crearTareaEnCasa(
        @Path("casaId") casaId: Long,
        @Body nuevaTarea: TareaRequest,
    ): Response<Tarea>

    @PUT("tareas/{tareaId}/notify")
    suspend fun notificarTarea(
        @Path("tareaId") tareaId: Long,
    ): Response<Void>

    @PUT("tareas/{tareaId}")
    suspend fun actualizarTarea(
        @Path("tareaId") tareaId: Long,
        @Body request: TareaRequest,
    ): Response<Tarea>

    @DELETE("tareas/{tareaId}")
    suspend fun borrarTarea(
        @Path("tareaId") tareaId: Long,
    ): Response<Void>

    @POST("tareas/{tareaId}/votar")
    suspend fun votarTarea(
        @Path("tareaId") tareaId: Long,
        @Query("usuarioId") usuarioId: Long,
        @Query("puntuacion") puntuacion: Int,
    ): retrofit2.Response<Void>

    @POST("tareas/repartir/casa/{casaId}")
    suspend fun ejecutarRepartoInteligente(
        @Path("casaId") casaId: Long,
    ): retrofit2.Response<okhttp3.ResponseBody>
}
