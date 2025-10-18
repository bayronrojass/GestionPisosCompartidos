package com.example.gestionpisoscompartidos.data.remote

import com.example.gestionpisoscompartidos.utils.ApiResult
import retrofit2.Response

class SafeApiCall {
    suspend fun <T> safeApiCall(apiCall: suspend () -> Response<T>): ApiResult<T> =
        try {
            val response = apiCall()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    ApiResult.Success(body)
                } else {
                    ApiResult.Error(response.code(), "Response body is null")
                }
            } else {
                ApiResult.Error(response.code(), response.message())
            }
        } catch (e: retrofit2.HttpException) {
            ApiResult.Error(e.code(), e.message())
        } catch (e: Exception) {
            ApiResult.Throws(e)
        }
}
