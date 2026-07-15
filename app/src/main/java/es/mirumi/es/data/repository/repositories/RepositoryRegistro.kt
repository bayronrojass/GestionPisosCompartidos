package es.mirumi.es.data.repository.repositories

import android.content.Context
import android.util.Log
import es.mirumi.es.data.remote.NetworkModule
import es.mirumi.es.data.repository.APIs.RegistroAPI
import es.mirumi.es.model.requests.RegistroRequest
import es.mirumi.es.model.responses.LoginResponse

class RepositoryRegistro(
    private val apiService: RegistroAPI,
    private val context: Context,
) {
    suspend fun register(request: RegistroRequest): LoginResponse {
        val response = apiService.register(request)
        val sharedPreferences = context.getSharedPreferences("fcm_prefs", Context.MODE_PRIVATE)
        val fcmToken = sharedPreferences.getString("fcm_token", null)

        if (response.isSuccessful) {
            val body = response.body() ?: throw Exception("Respuesta de registro vacía o inválida")

            // Auto-login: persist the freshly-minted JWT so the interceptor picks it up on
            // subsequent requests. Mirrors RepositoryLogin.login post-success handling.
            NetworkModule.sessionManager.saveAuthData(
                body.authToken,
                body.user.id,
                body.user.correo,
            )
            if (fcmToken != null) {
                val repository = RepositoryUsuario(NetworkModule.usuarioApiService)
                repository.updateUsuarioToken(body.user.id, fcmToken.replace("\"", ""))
                Log.d("TOKEN", "FCM token registrado tras registro")
            }
            return body
        }

        val errorBody = response.errorBody()?.string() ?: "Error desconocido"
        val message =
            when (response.code()) {
                400 -> "Datos inválidos. Revisa los campos e inténtalo de nuevo."
                409 -> "Ese correo ya está registrado."
                else -> "Error del servidor: ${response.code()}. Detalle: $errorBody"
            }
        throw Exception(message)
    }
}
