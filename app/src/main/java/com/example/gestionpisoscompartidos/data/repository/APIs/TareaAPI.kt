package com.example.gestionpisoscompartidos.data.repository.APIs

import com.example.gestionpisoscompartidos.model.Tarea
import com.example.gestionpisoscompartidos.model.TareaRequest
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
