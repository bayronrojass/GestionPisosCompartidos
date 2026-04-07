package es.mirumi.es.data

import android.content.Context
import android.content.SharedPreferences

/**
 * Gestiona la sesión del usuario, almacenando y recuperando datos clave
 * como el token de autenticación, el ID de usuario y la vivienda activa.
 */
class SessionManager(
    context: Context,
) {
    companion object {
        private const val PREFS_FILENAME = "es.mirumi.es.AUTH_PREFS"

        // Claves
        private const val KEY_AUTH_TOKEN = "AUTH_TOKEN"
        private const val KEY_USER_ID = "USER_ID"
        private const val KEY_USER_EMAIL = "USER_EMAIL"
        private const val KEY_CASA_ACTIVA_ID = "CASA_ACTIVA_ID"
    }

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)

    fun saveAuthData(
        token: String,
        userId: Long,
        email: String,
    ) {
        prefs.edit().apply {
            putString(KEY_AUTH_TOKEN, token)
            putLong(KEY_USER_ID, userId)
            putString(KEY_USER_EMAIL, email)
            apply()
        }
    }

    fun fetchAuthToken(): String? {
        val token = prefs.getString(KEY_AUTH_TOKEN, null)
        return if (token != null) "Bearer $token" else null
    }

    fun fetchCurrentUserId(): Long = prefs.getLong(KEY_USER_ID, -1L)

    fun fetchUserEmail(): String? = prefs.getString(KEY_USER_EMAIL, null)

    fun isLoggedIn(): Boolean = prefs.getString(KEY_AUTH_TOKEN, null) != null

    fun logoutUser() {
        // Borramos los datos de sesión, PERO mantenemos la última casa guardada
        prefs.edit().apply {
            remove(KEY_AUTH_TOKEN)
            remove(KEY_USER_ID)
            remove(KEY_USER_EMAIL)
            apply()
        }
    }

    fun saveCasaActivaId(casaId: Long) {
        prefs.edit().putLong(KEY_CASA_ACTIVA_ID, casaId).apply()
    }

    fun getCasaActivaId(): Long = prefs.getLong(KEY_CASA_ACTIVA_ID, -1L)

    fun clearCasaActiva() {
        prefs.edit().remove(KEY_CASA_ACTIVA_ID).apply()
    }
}
