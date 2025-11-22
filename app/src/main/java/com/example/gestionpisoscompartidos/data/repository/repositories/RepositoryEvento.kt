package com.example.gestionpisoscompartidos.data.repository.repositories

import android.content.ContentResolver
import com.example.gestionpisoscompartidos.data.repository.APIs.EventoAPI
import com.example.gestionpisoscompartidos.model.eventRequest
import com.example.gestionpisoscompartidos.model.eventoResponse
import com.google.gson.Gson

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
}
