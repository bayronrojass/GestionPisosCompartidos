package es.mirumi.es.data.repository.repositories

import android.util.Log
import es.mirumi.es.data.remote.NetworkModule
import es.mirumi.es.data.remote.RemoteRepository
import es.mirumi.es.data.repository.APIs.CasaAPI
import es.mirumi.es.utils.ApiResult

class RepositoryLienzo {
    private val repository = RemoteRepository(NetworkModule.retrofit.create(CasaAPI::class.java))

    suspend fun getLienzo(casaId: Long): Long? {
        val lienzoResponse = repository.request { getLienzo(casaId) }

        when (lienzoResponse) {
            is ApiResult.Error -> {
                Log.e(
                    "Pizarra",
                    "Error when loading lienzo " + lienzoResponse.message + lienzoResponse.code
                )
                return null
            }

            is ApiResult.Success<Long> -> {
                return lienzoResponse.data
            }

            is ApiResult.Throws -> {
                Log.e("Pizarra", "Throws when loading lienzo " + lienzoResponse.exception.message)
                return null
            }
        }
    }
}
