package ru.sersb.wgsingbox.util

import io.jsonwebtoken.ExpiredJwtException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import ru.sersb.wgsingbox.model.entity.User
import ru.sersb.wgsingbox.model.enum.UserRole
import java.time.LocalDateTime

class JwtTokenProviderTest {

    private lateinit var jwtTokenProvider: JwtTokenProvider
    private lateinit var testUser: User

    private val secret = "test-secret-key-for-testing-purposes-only-must-be-at-least-256-bits-long-for-hs512"
    private val expiration = 3600000L // 1 hour
    private val refreshExpiration = 86400000L // 24 hours

    @BeforeEach
    fun setUp() {
        jwtTokenProvider = JwtTokenProvider(
            secret = secret,
            expiration = expiration,
            refreshExpiration = refreshExpiration
        )

        testUser = User(
            username = "testuser",
            password = "password",
            email = "test@example.com",
            role = UserRole.USER
        )
        testUser.id = 1L
    }

    @Test
    fun `generateToken should return valid JWT`() {
        val token = jwtTokenProvider.generateToken(testUser)

        assertNotNull(token)
        assertTrue(token.isNotEmpty())
        assertTrue(token.contains(".")) // JWT has 3 parts separated by dots
    }

    @Test
    fun `getUserIdFromToken should return correct user id`() {
        val token = jwtTokenProvider.generateToken(testUser)
        val userId = jwtTokenProvider.getUserIdFromToken(token)

        assertEquals(1L, userId)
    }

    @Test
    fun `getUsernameFromToken should return correct username`() {
        val token = jwtTokenProvider.generateToken(testUser)
        val username = jwtTokenProvider.getUsernameFromToken(token)

        assertEquals("testuser", username)
    }

    @Test
    fun `getRoleFromToken should return correct role`() {
        val token = jwtTokenProvider.generateToken(testUser)
        val role = jwtTokenProvider.getRoleFromToken(token)

        assertEquals("USER", role)
    }

    @Test
    fun `validateToken should return true for valid token`() {
        val token = jwtTokenProvider.generateToken(testUser)
        val isValid = jwtTokenProvider.validateToken(token)

        assertTrue(isValid)
    }

    @Test
    fun `validateToken should return false for invalid token`() {
        val invalidToken = "invalid.jwt.token"
        val isValid = jwtTokenProvider.validateToken(invalidToken)

        assertFalse(isValid)
    }

    @Test
    fun `generateRefreshToken should return valid token`() {
        val token = jwtTokenProvider.generateRefreshToken(testUser)

        assertNotNull(token)
        assertTrue(token.isNotEmpty())
        assertTrue(token.contains("."))
    }

    @Test
    fun `getUserIdFromToken should return null for invalid token`() {
        val invalidToken = "invalid.jwt.token"
        val userId = jwtTokenProvider.getUserIdFromToken(invalidToken)

        assertNull(userId)
    }

    @Test
    fun `getUsernameFromToken should return null for invalid token`() {
        val invalidToken = "invalid.jwt.token"
        val username = jwtTokenProvider.getUsernameFromToken(invalidToken)

        assertNull(username)
    }

    @Test
    fun `getRoleFromToken should return null for invalid token`() {
        val invalidToken = "invalid.jwt.token"
        val role = jwtTokenProvider.getRoleFromToken(invalidToken)

        assertNull(role)
    }

    @Test
    fun `tokens should work with admin role`() {
        val adminUser = User(
            username = "admin",
            password = "password",
            email = "admin@example.com",
            role = UserRole.ADMIN
        )
        adminUser.id = 2L

        val token = jwtTokenProvider.generateToken(adminUser)
        val userId = jwtTokenProvider.getUserIdFromToken(token)
        val username = jwtTokenProvider.getUsernameFromToken(token)
        val role = jwtTokenProvider.getRoleFromToken(token)

        assertEquals(2L, userId)
        assertEquals("admin", username)
        assertEquals("ADMIN", role)
    }
}
