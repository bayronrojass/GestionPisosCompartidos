package es.mirumi.es.data.repository.APIs

import es.mirumi.es.model.Tarea
import es.mirumi.es.model.requests.TareaRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface TareaAPI {
    @GET("casas/{casaId}/tareas")
    suspend fun getTareasByCasaId(
        @Path("casaId") casaId: Long,
    ): Response<List<Tarea>>

    @POST("casas/{casaId}/tareas")
    suspend fun crearTareaEnCasa(
        @Path("casaId") casaId: Long,
        @Body nuevaTarea: TareaRequest,
    ): Response<Tarea>

    @POST("tareas/{tareaId}/notify")
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
}
