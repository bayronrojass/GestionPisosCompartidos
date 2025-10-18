package com.example.gestionpisoscompartidos.data.remote

import com.example.gestionpisoscompartidos.utils.ApiResult
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
