package es.mirumi.es.data.repository.repositories

import android.content.ContentResolver
import com.google.gson.Gson
import es.mirumi.es.data.repository.APIs.EventoAPI
import es.mirumi.es.model.requests.eventRequest
import es.mirumi.es.model.responses.eventoResponse

class RepositoryEvento(
    private val apiService: EventoAPI,
) {
    suspend fun crearEvento(
        request: eventRequest,
        casaId: Long,
        contentResolver: ContentResolver,
    ): eventoResponse {
        val debugJson = Gson().toJson(request)
        println("JSON enviado: $debugJson")

        try {
            val response = apiService.crearEvento(casaId, request)
            if (response.isSuccessful) {
                val responseBody = response.body()
                println("Response body: $responseBody")
                return responseBody ?: throw Exception("Respuesta vacía al crear evento")
            } else {
                val errorBody = response.errorBody()?.string()
                println("Error body: $errorBody")
                throw Exception("Error al crear evento (${response.code()}): $errorBody")
            }
        } catch (e: Exception) {
            println("Exception details: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }

    suspend fun eliminarEvento(eventoId: Long): Boolean {
        try {
            val response = apiService.eliminarEvento(eventoId)
            return response.isSuccessful
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun actualizarEvento(
        eventoId: Long,
        request: eventRequest,
    ): Boolean {
        try {
            val response = apiService.actualizarEvento(eventoId, request)
            return response.isSuccessful
        } catch (e: Exception) {
            throw e
        }
    }
}
