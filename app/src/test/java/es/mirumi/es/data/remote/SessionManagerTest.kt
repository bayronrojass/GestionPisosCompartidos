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
    private val context: Context = ApplicationProvider.getApplicationContext()
    private lateinit var sessionManager: SessionManager

    @Before
    fun setUp() {
        val prefs = context.getSharedPreferences("es.mirumi.es.AUTH_PREFS_TEST", Context.MODE_PRIVATE)
        sessionManager = SessionManager(prefs)
        sessionManager.logoutUser()
    }

    @After
    fun tearDown() {
        sessionManager.logoutUser()
    }

    @Test
    fun `fetchAuthToken successful retrieval`() {
        sessionManager.saveAuthData("my-secret-token", 1L, "user@test.com")
        val token = sessionManager.fetchAuthToken()
        assertEquals("Bearer my-secret-token", token)
    }

    @Test
    fun `fetchAuthToken no session`() {
        val token = sessionManager.fetchAuthToken()
        assertNull(token)
    }

    @Test
    fun `fetchAuthToken after logout`() {
        sessionManager.saveAuthData("token-to-be-deleted", 1L, "user@test.com")
        sessionManager.logoutUser()
        val token = sessionManager.fetchAuthToken()
        assertNull(token)
    }

    @Test
    fun `saveAuthData successful save and verify`() {
        val token = "valid-token-456"
        val userId = 123L
        val email = "test.user@example.com"
        sessionManager.saveAuthData(token, userId, email)
        assertEquals("Bearer $token", sessionManager.fetchAuthToken())
        assertEquals(userId, sessionManager.fetchCurrentUserId())
        assertEquals(email, sessionManager.fetchUserEmail())
        assertTrue(sessionManager.isLoggedIn())
    }

    @Test
    fun `saveAuthData with empty token`() {
        sessionManager.saveAuthData("", 1L, "user@test.com")
        assertEquals("Bearer ", sessionManager.fetchAuthToken())
        assertTrue(sessionManager.isLoggedIn())
    }

    @Test
    fun `saveAuthData with empty email`() {
        sessionManager.saveAuthData("some-token", 1L, "")
        assertEquals("", sessionManager.fetchUserEmail())
    }

    @Test
    fun `saveAuthData overwrite existing data`() {
        sessionManager.saveAuthData("old-token", 1L, "old@email.com")
        val newToken = "new-shiny-token"
        val newUserId = 2L
        val newEmail = "new@email.com"
        sessionManager.saveAuthData(newToken, newUserId, newEmail)
        assertEquals("Bearer $newToken", sessionManager.fetchAuthToken())
        assertEquals(newUserId, sessionManager.fetchCurrentUserId())
        assertEquals(newEmail, sessionManager.fetchUserEmail())
    }

    @Test
    fun `logoutUser clears all data`() {
        sessionManager.saveAuthData("token-to-clear", 10L, "clear@me.com")
        sessionManager.logoutUser()
        assertNull(sessionManager.fetchAuthToken())
        assertEquals(-1L, sessionManager.fetchCurrentUserId())
        assertNull(sessionManager.fetchUserEmail())
        assertFalse(sessionManager.isLoggedIn())
    }

    @Test
    fun `logoutUser when no session exists`() {
        sessionManager.logoutUser()
        assertNull(sessionManager.fetchAuthToken())
        assertEquals(-1L, sessionManager.fetchCurrentUserId())
        assertFalse(sessionManager.isLoggedIn())
    }

    @Test
    fun `fetchCurrentUserId successful retrieval`() {
        sessionManager.saveAuthData("token", 999L, "user@test.com")
        assertEquals(999L, sessionManager.fetchCurrentUserId())
    }

    @Test
    fun `fetchCurrentUserId no session`() {
        assertEquals(-1L, sessionManager.fetchCurrentUserId())
    }

    @Test
    fun `fetchCurrentUserId with zero as id`() {
        sessionManager.saveAuthData("token", 0L, "zero@id.com")
        assertEquals(0L, sessionManager.fetchCurrentUserId())
    }

    @Test
    fun `fetchUserEmail successful retrieval`() {
        val email = "my.email@domain.com"
        sessionManager.saveAuthData("token", 1L, email)
        assertEquals(email, sessionManager.fetchUserEmail())
    }

    @Test
    fun `fetchUserEmail no session`() {
        assertNull(sessionManager.fetchUserEmail())
    }

    @Test
    fun `isLoggedIn user is logged in`() {
        sessionManager.saveAuthData("a-valid-token", 1L, "user@test.com")
        assertTrue(sessionManager.isLoggedIn())
    }

    @Test
    fun `isLoggedIn user is not logged in`() {
        assertFalse(sessionManager.isLoggedIn())
    }

    @Test
    fun `isLoggedIn after logout`() {
        sessionManager.saveAuthData("a-token", 1L, "user@test.com")
        sessionManager.logoutUser()
        assertFalse(sessionManager.isLoggedIn())
    }

    @Test
    fun `isLoggedIn with empty token`() {
        sessionManager.saveAuthData("", 1L, "user@test.com")
        assertTrue(sessionManager.isLoggedIn())
    }

    @Test
    fun `multi instance isolation`() {
        val userPrefs = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val adminPrefs = context.getSharedPreferences("admin_session", Context.MODE_PRIVATE)
        userPrefs.edit().putString("USER_TOKEN", "user_token_123").apply()
        adminPrefs.edit().putString("USER_TOKEN", "admin_token_456").apply()
        val userToken = userPrefs.getString("USER_TOKEN", null)
        val adminToken = adminPrefs.getString("USER_TOKEN", null)
        assertNotEquals(userToken, adminToken)
        assertEquals("user_token_123", userToken)
        assertEquals("admin_token_456", adminToken)
    }
}
