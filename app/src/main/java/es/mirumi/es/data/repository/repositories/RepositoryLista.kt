package es.mirumi.es.data.repository.repositories

import es.mirumi.es.data.repository.APIs.ListaAPI
import es.mirumi.es.model.Lista
import es.mirumi.es.model.requests.ListaRequest

class RepositoryLista(
    private val apiService: ListaAPI,
) {
    suspend fun getListasByCasaId(casaId: Long): List<Lista> {
        val response = apiService.getListasByCasaId(casaId)
        if (response.isSuccessful) {
            return response.body() ?: throw Exception("Respuesta de listas vacía")
        } else {
            val errorBody = response.errorBody()?.string() ?: "Error desconocido"
            throw Exception("Error al obtener listas (${response.code()}): $errorBody")
        }
    }

    suspend fun crearListaEnCasa(
        casaId: Long,
        request: ListaRequest,
    ): Lista {
        val response = apiService.crearListaEnCasa(casaId, request)
        if (response.isSuccessful) {
            return response.body() ?: throw Exception("Respuesta de creación vacía")
        } else {
            val errorBody = response.errorBody()?.string() ?: "Error desconocido"
            throw Exception("Error al crear lista (${response.code()}): $errorBody")
        }
    }

    suspend fun borrarLista(listaId: Long) {
        val response = apiService.borrarLista(listaId)
        if (!response.isSuccessful) {
            val errorBody = response.errorBody()?.string() ?: "Error desconocido"
            throw Exception("Error al borrar lista (${response.code()}): $errorBody")
        }
        // No devuelve nada si es exitoso (204 No Content)
    }
}
