package es.mirumi.es.data.repository.repositories

import android.content.Context
import android.util.Log
import es.mirumi.es.data.remote.NetworkModule
import es.mirumi.es.data.repository.APIs.LoginAPI
import es.mirumi.es.model.requests.LoginRequest
import es.mirumi.es.model.responses.LoginResponse

class RepositoryLogin(
    private val apiService: LoginAPI,
    private val context: Context,
) {
    suspend fun login(request: LoginRequest): LoginResponse {
        val response = apiService.login(request)
        val sharedPreferences = context.getSharedPreferences("fcm_prefs", Context.MODE_PRIVATE)
        val fcmToken = sharedPreferences.getString("fcm_token", null)

        if (response.isSuccessful) {
            if (fcmToken != null && response.body()?.user?.id != null) {
                val repository = RepositoryUsuario(NetworkModule.usuarioApiService)
                repository.updateUsuarioToken(response.body()!!.user.id, fcmToken.toString().replace("\"", ""))
                Log.d("TOKEN", "Token creado")
                return response.body() ?: throw Exception("Respuesta de login vacía o inválida")
            }
            Log.d("TOKEN", "Token no creado")
            return response.body() ?: throw Exception("Respuesta de login vacía o inválida")
        } else {
            val errorBody = response.errorBody()?.string() ?: "Error desconocido"
            val errorMessage =
                when (response.code()) {
                    401 -> "Credenciales incorrectas."
                    404 -> "Endpoint no encontrado en el servidor."
                    else -> "Error del servidor: ${response.code()}. Detalle: $errorBody"
                }
            throw Exception(errorMessage)
        }
    }
}
