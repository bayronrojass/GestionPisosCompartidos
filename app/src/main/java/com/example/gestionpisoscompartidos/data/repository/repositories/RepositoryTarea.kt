package com.example.gestionpisoscompartidos.data.repository.repositories

import com.example.gestionpisoscompartidos.data.repository.APIs.TareaAPI
import com.example.gestionpisoscompartidos.model.Tarea
import com.example.gestionpisoscompartidos.model.TareaRequest

class RepositoryTarea(
    private val apiService: TareaAPI,
) {
    suspend fun getTareasByCasaId(casaId: Long): List<Tarea> {
        val response = apiService.getTareasByCasaId(casaId)
        if (response.isSuccessful) {
            return response.body() ?: emptyList()
        } else {
            throw Exception("Error al obtener tareas: ${response.message()}")
        }
    }

    suspend fun crearTarea(
        casaId: Long,
        request: TareaRequest,
    ): Tarea {
        val response = apiService.crearTareaEnCasa(casaId, request)
        if (response.isSuccessful) {
            return response.body() ?: throw Exception("Respuesta vacía al crear")
        } else {
            throw Exception("Error al crear tarea: ${response.message()}")
        }
    }

    suspend fun actualizarTarea(
        tareaId: Long,
        request: TareaRequest,
    ): Tarea {
        val response = apiService.actualizarTarea(tareaId, request)
        if (response.isSuccessful) {
            return response.body() ?: throw Exception("Respuesta vacía al actualizar")
        } else {
            throw Exception("Error al actualizar tarea: ${response.message()}")
        }
    }

    suspend fun borrarTarea(tareaId: Long) {
        val response = apiService.borrarTarea(tareaId)
        if (!response.isSuccessful) {
            throw Exception("Error al borrar tarea: ${response.message()}")
        }
    }
}
