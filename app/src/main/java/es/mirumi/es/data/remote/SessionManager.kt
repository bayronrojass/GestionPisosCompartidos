package es.mirumi.es.data

import android.content.Context
import android.content.SharedPreferences

class SessionManager(
    context: Context,
) {
    companion object {
        private const val PREFS_FILENAME = "es.mirumi.es.AUTH_PREFS"
        private const val KEY_AUTH_TOKEN = "AUTH_TOKEN"
        private const val KEY_USER_ID = "USER_ID"
        private const val KEY_USER_EMAIL = "USER_EMAIL"
        private const val KEY_CASA_ACTIVA_ID = "CASA_ACTIVA_ID"
        private const val KEY_CASA_ACTIVA_NOMBRE = "CASA_ACTIVA_NOMBRE"
        private const val KEY_BIOMETRIC_ENABLED = "BIOMETRIC_ENABLED"
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

    fun setBiometricEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
    }

    fun isBiometricEnabled(): Boolean = prefs.getBoolean(KEY_BIOMETRIC_ENABLED, false)

    fun hasSavedToken(): Boolean = prefs.getString(KEY_AUTH_TOKEN, null) != null

    fun fetchAuthToken(): String? {
        val token = prefs.getString(KEY_AUTH_TOKEN, null)
        return if (token != null) "Bearer $token" else null
    }

    fun fetchCurrentUserId(): Long = prefs.getLong(KEY_USER_ID, -1L)

    fun fetchUserEmail(): String? = prefs.getString(KEY_USER_EMAIL, null)

    fun isLoggedIn(): Boolean = fetchAuthToken() != null

    fun logoutUser() {
        prefs.edit().apply {
            remove(KEY_AUTH_TOKEN)
            remove(KEY_USER_ID)
            remove(KEY_USER_EMAIL)
            remove(KEY_CASA_ACTIVA_ID)
            remove(KEY_CASA_ACTIVA_NOMBRE)
            remove(KEY_BIOMETRIC_ENABLED)
            apply()
        }
    }

    fun saveCasaActiva(
        casaId: Long,
        casaNombre: String,
    ) {
        prefs.edit().apply {
            putLong(KEY_CASA_ACTIVA_ID, casaId)
            putString(KEY_CASA_ACTIVA_NOMBRE, casaNombre)
            apply()
        }
    }

    fun getCasaActivaId(): Long = prefs.getLong(KEY_CASA_ACTIVA_ID, -1L)

    fun getCasaActivaNombre(): String = prefs.getString(KEY_CASA_ACTIVA_NOMBRE, "Mi Piso") ?: "Mi Piso"

    fun clearCasaActiva() {
        prefs
            .edit()
            .remove(KEY_CASA_ACTIVA_ID)
            .remove(KEY_CASA_ACTIVA_NOMBRE)
            .apply()
    }
}
