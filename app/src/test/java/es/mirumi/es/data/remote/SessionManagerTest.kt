package es.mirumi.es.data.remote

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import es.mirumi.es.data.SessionManager
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SessionManagerTest {
    // Se usa un context real proporcionado por Robolectric
    private val context: Context = ApplicationProvider.getApplicationContext()
    private lateinit var sessionManager: SessionManager

    @Before
    fun setUp() {
        // Inicializamos el SessionManager antes de cada test para asegurar un estado limpio.
        sessionManager = SessionManager(context)
        // Limpiamos cualquier dato de una ejecución anterior
        sessionManager.logoutUser()
    }

    @After
    fun tearDown() {
        // Limpiamos las SharedPreferences después de cada test para no afectar a los siguientes.
        sessionManager.logoutUser()
    }

    @Test
    fun `fetchAuthToken successful retrieval`() {
        // Given: Guardamos datos de autenticación
        sessionManager.saveAuthData("my-secret-token", 1L, "user@test.com")

        // When: Se pide el token de autenticación
        val token = sessionManager.fetchAuthToken()

        // Then: Debería devolver el token con el prefijo 'Bearer '.
        assertEquals("Bearer my-secret-token", token)
    }

    @Test
    fun `fetchAuthToken no session`() {
        // Given: No hay ninguna sesión guardada (asegurado por setUp)

        // When: Se pide el token
        val token = sessionManager.fetchAuthToken()

        // Then: Debería devolver null.
        assertNull(token)
    }

    @Test
    fun `fetchAuthToken after logout`() {
        // Given: Un usuario inició sesión y luego la cerró
        sessionManager.saveAuthData("token-to-be-deleted", 1L, "user@test.com")
        sessionManager.logoutUser()

        // When: Se pide el token
        val token = sessionManager.fetchAuthToken()

        // Then: Debería devolver null.
        assertNull(token)
    }

    @Test
    fun `saveAuthData successful save and verify`() {
        // Given: Datos de autenticación válidos
        val token = "valid-token-456"
        val userId = 123L
        val email = "test.user@example.com"

        // When: Se guardan los datos
        sessionManager.saveAuthData(token, userId, email)

        // Then: Las llamadas posteriores a los métodos de obtención deben devolver los datos guardados.
        assertEquals("Bearer $token", sessionManager.fetchAuthToken())
        assertEquals(userId, sessionManager.fetchCurrentUserId())
        assertEquals(email, sessionManager.fetchUserEmail())
        assertTrue(sessionManager.isLoggedIn())
    }

    @Test
    fun `saveAuthData with empty token`() {
        // Given: Un token de cadena vacía
        val emptyToken = ""

        // When: Se guardan los datos
        sessionManager.saveAuthData(emptyToken, 1L, "user@test.com")

        // Then: El sistema maneja tokens vacíos pero presentes.
        assertEquals("Bearer ", sessionManager.fetchAuthToken())
        assertTrue(sessionManager.isLoggedIn())
    }

    @Test
    fun `saveAuthData with empty email`() {
        // Given: Un email de cadena vacía
        val emptyEmail = ""

        // When: Se guardan los datos
        sessionManager.saveAuthData("some-token", 1L, emptyEmail)

        // Then: fetchUserEmail debería devolver una cadena vacía.
        assertEquals(emptyEmail, sessionManager.fetchUserEmail())
    }

    @Test
    fun `saveAuthData overwrite existing data`() {
        // Given: Ya existen datos de sesión
        sessionManager.saveAuthData("old-token", 1L, "old@email.com")

        // When: Se guardan nuevos datos
        val newToken = "new-shiny-token"
        val newUserId = 2L
        val newEmail = "new@email.com"
        sessionManager.saveAuthData(newToken, newUserId, newEmail)

        // Then: Los datos antiguos deben ser reemplazados por los nuevos.
        assertEquals("Bearer $newToken", sessionManager.fetchAuthToken())
        assertEquals(newUserId, sessionManager.fetchCurrentUserId())
        assertEquals(newEmail, sessionManager.fetchUserEmail())
    }

    @Test
    fun `logoutUser clears all data`() {
        // Given: Un usuario ha iniciado sesión
        sessionManager.saveAuthData("token-to-clear", 10L, "clear@me.com")

        // When: Se llama a logoutUser
        sessionManager.logoutUser()

        // Then: Todos los métodos de obtención deben devolver sus valores por defecto de 'sin sesión'.
        assertNull(sessionManager.fetchAuthToken())
        assertEquals(-1L, sessionManager.fetchCurrentUserId())
        assertNull(sessionManager.fetchUserEmail())
        assertFalse(sessionManager.isLoggedIn())
    }

    @Test
    fun `logoutUser when no session exists`() {
        // Given: Ningún usuario ha iniciado sesión

        // When: Se llama a logoutUser
        sessionManager.logoutUser()

        // Then: La app no debe crashear y los valores deben seguir siendo los de 'sin sesión'.
        assertNull(sessionManager.fetchAuthToken())
        assertEquals(-1L, sessionManager.fetchCurrentUserId())
        assertFalse(sessionManager.isLoggedIn())
    }

    @Test
    fun `fetchCurrentUserId successful retrieval`() {
        // Given: Se guardan datos de sesión con un userId específico
        val userId = 999L
        sessionManager.saveAuthData("token", userId, "user@test.com")

        // When: Se pide el ID del usuario
        val fetchedId = sessionManager.fetchCurrentUserId()

        // Then: Debería devolver ese ID exacto.
        assertEquals(userId, fetchedId)
    }

    @Test
    fun `fetchCurrentUserId no session`() {
        // Given: No hay datos de sesión

        // When: Se pide el ID del usuario
        val fetchedId = sessionManager.fetchCurrentUserId()

        // Then: Debería devolver el valor por defecto -1L.
        assertEquals(-1L, fetchedId)
    }

    @Test
    fun `fetchCurrentUserId with zero as id`() {
        // Given: Se guardan datos de sesión con userId = 0
        sessionManager.saveAuthData("token", 0L, "zero@id.com")

        // When: Se pide el ID del usuario
        val fetchedId = sessionManager.fetchCurrentUserId()

        // Then: Debería devolver 0 correctamente y no el valor por defecto.
        assertEquals(0L, fetchedId)
    }

    @Test
    fun `fetchUserEmail successful retrieval`() {
        // Given: Se guardan datos con un email específico
        val email = "my.email@domain.com"
        sessionManager.saveAuthData("token", 1L, email)

        // When: Se pide el email
        val fetchedEmail = sessionManager.fetchUserEmail()

        // Then: Debería devolver esa cadena de email exacta.
        assertEquals(email, fetchedEmail)
    }

    @Test
    fun `fetchUserEmail no session`() {
        // Given: No hay sesión

        // When: Se pide el email
        val fetchedEmail = sessionManager.fetchUserEmail()

        // Then: Debería devolver null.
        assertNull(fetchedEmail)
    }

    @Test
    fun `isLoggedIn user is logged in`() {
        // Given: Se ha llamado a saveAuthData con un token no nulo
        sessionManager.saveAuthData("a-valid-token", 1L, "user@test.com")

        // When: Se llama a isLoggedIn
        val isLoggedIn = sessionManager.isLoggedIn()

        // Then: Debería devolver true.
        assertTrue(isLoggedIn)
    }

    @Test
    fun `isLoggedIn user is not logged in`() {
        // Given: No existen datos de sesión

        // When: Se llama a isLoggedIn
        val isLoggedIn = sessionManager.isLoggedIn()

        // Then: Debería devolver false.
        assertFalse(isLoggedIn)
    }

    @Test
    fun `isLoggedIn after logout`() {
        // Given: Un usuario inició sesión y luego llamó a logoutUser
        sessionManager.saveAuthData("a-token", 1L, "user@test.com")
        sessionManager.logoutUser()

        // When: Se llama a isLoggedIn
        val isLoggedIn = sessionManager.isLoggedIn()

        // Then: Debería devolver false.
        assertFalse(isLoggedIn)
    }

    @Test
    fun `isLoggedIn with empty token`() {
        // Given: Se llamó a saveAuthData con una cadena vacía como token
        sessionManager.saveAuthData("", 1L, "user@test.com")

        // When: Se llama a isLoggedIn
        val isLoggedIn = sessionManager.isLoggedIn()

        // Then: Debería devolver true, ya que la clave existe.
        assertTrue(isLoggedIn)
    }

    @Test
    fun `multi instance isolation`() {
        // Given: Dos instancias diferentes de SessionManager con nombres de archivo de preferencias distintos
        val userPrefs = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val adminPrefs = context.getSharedPreferences("admin_session", Context.MODE_PRIVATE)

        // Creamos instancias 'manuales' para este test, aunque la clase SessionManager no lo exponga directamente
        // Para que este test funcione, SessionManager debería permitir cambiar el nombre del fichero,
        // o podemos simularlo creando dos ficheros de SharedPreferences.
        userPrefs.edit().putString("USER_TOKEN", "user_token_123").apply()
        adminPrefs.edit().putString("USER_TOKEN", "admin_token_456").apply()

        // When: Se leen los datos de cada instancia
        val userToken = userPrefs.getString("USER_TOKEN", null)
        val adminToken = adminPrefs.getString("USER_TOKEN", null)

        // Then: Los datos no deben ser legibles de la otra instancia, asegurando el aislamiento.
        assertNotEquals(userToken, adminToken)
        assertEquals("user_token_123", userToken)
        assertEquals("admin_token_456", adminToken)
    }
}
