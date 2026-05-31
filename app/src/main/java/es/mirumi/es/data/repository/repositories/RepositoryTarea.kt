package es.mirumi.es.data.repository.repositories

import android.util.Log
import es.mirumi.es.data.repository.APIs.TareaAPI
import es.mirumi.es.model.Tarea
import es.mirumi.es.model.requests.TareaRequest

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

    suspend fun notificarTarea(tareaId: Long) {
        val response = apiService.notificarTarea(tareaId)
        if (!response.isSuccessful) {
            val code = response.code()
            Log.e("notificarTarea", "Error HTTP $code: ${response.message()}")
            throw Exception("No se pudo enviar el recordatorio (HTTP $code)")
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
            val code = response.code()
            val errorBody = response.errorBody()?.string()
            Log.e("crearTarea", "Error HTTP $code: ${response.message()} - $errorBody")
            throw Exception("Error al crear tarea (HTTP $code): ${response.message()} - $errorBody")
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
