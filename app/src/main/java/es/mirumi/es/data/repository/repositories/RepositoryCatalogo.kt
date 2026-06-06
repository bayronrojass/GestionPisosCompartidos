package es.mirumi.es.data.repository.repositories

import android.util.Log
import es.mirumi.es.data.repository.APIs.CatalogoAPI
import es.mirumi.es.model.CatalogoProducto

class RepositoryCatalogo(
    private val apiService: CatalogoAPI,
) {
    suspend fun buscarProductos(query: String): List<CatalogoProducto> =
        try {
            val response = apiService.buscarProductos(query)
            if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
        } catch (e: Exception) {
            Log.e("RepositoryCatalogo", "Error al buscar: ${e.message}")
            emptyList()
        }

    suspend fun getPopulares(): List<CatalogoProducto> =
        try {
            val response = apiService.getPopulares()
            if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
        } catch (e: Exception) {
            Log.e("RepositoryCatalogo", "Error al cargar populares: ${e.message}")
            emptyList()
        }
}
