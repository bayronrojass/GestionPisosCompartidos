package com.example.gestionpisoscompartidos.data

import android.content.Context
import android.content.SharedPreferences

/**
 * Gestiona la sesión del usuario, almacenando y recuperando datos clave
 * como el token de autenticación y el ID de usuario.
 */
class SessionManager(context: Context) {

    // 1. Define el nombre del archivo de preferencias y las claves
    companion object {
        private const val PREFS_FILENAME = "com.example.app.AUTH_PREFS"
        private const val KEY_AUTH_TOKEN = "AUTH_TOKEN"
        private const val KEY_USER_ID = "USER_ID"
        private const val KEY_USER_EMAIL = "USER_EMAIL" // Útil para mostrarlo
    }

    // 2. Inicializa SharedPreferences
    // Usamos context.applicationContext para evitar fugas de memoria
    private val prefs: SharedPreferences = context.applicationContext.getSharedPreferences(
        PREFS_FILENAME,
        Context.MODE_PRIVATE
    )

    /**
     * Guarda los datos de la sesión del usuario.
     * Esto debe ser llamado DESPUÉS de un inicio de sesión exitoso.
     *
     * @param token El token JWT recibido del backend (sin el prefijo "Bearer ").
     * @param userId El ID del usuario.
     * @param email El email del usuario.
     */
    fun saveAuthData(token: String, userId: Long, email: String) {
        val editor = prefs.edit()
        editor.putString(KEY_AUTH_TOKEN, token)
        editor.putLong(KEY_USER_ID, userId)
        editor.putString(KEY_USER_EMAIL, email)
        editor.apply() // .apply() lo hace en segundo plano
    }

    /**
     * Recupera el token de autenticación formateado para las cabeceras de API.
     *
     * @return "Bearer [TOKEN]" si existe, o null si no hay sesión.
     */
    fun fetchAuthToken(): String? {
        val token = prefs.getString(KEY_AUTH_TOKEN, null)
        return if (token != null) {
            "Bearer $token"
        } else {
            null
        }
    }

    /**
     * Recupera el ID del usuario actualmente logueado.
     *
     * @return El ID del usuario, o -1 si no hay sesión.
     */
    fun fetchCurrentUserId(): Long {
        return prefs.getLong(KEY_USER_ID, -1L)
    }

    /**
     * Recupera el email del usuario actualmente logueado.
     *
     * @return El email del usuario, o null si no hay sesión.
     */
    fun fetchUserEmail(): String? {
        return prefs.getString(KEY_USER_EMAIL, null)
    }

    /**
     * Comprueba si el usuario está actualmente logueado.
     *
     * @return true si existe un token, false en caso contrario.
     */
    fun isLoggedIn(): Boolean {
        return prefs.getString(KEY_AUTH_TOKEN, null) != null
    }

    /**
     * Borra todos los datos de la sesión.
     * Esto debe ser llamado al cerrar sesión.
     */
    fun logoutUser() {
        val editor = prefs.edit()
        editor.clear() // Borra todas las claves
        editor.apply()
    }
}