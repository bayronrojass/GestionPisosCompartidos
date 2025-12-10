package es.mirumi.es.data.remote

import es.mirumi.es.utils.ApiResult
import retrofit2.Response

class RemoteRepository<T : Any>(
    private val api: T,
) {
    private val safeApiCall = SafeApiCall()

    suspend fun <R> request(call: suspend T.() -> Response<R>): ApiResult<R> =
        safeApiCall.safeApiCall {
            api.call()
        }
}
