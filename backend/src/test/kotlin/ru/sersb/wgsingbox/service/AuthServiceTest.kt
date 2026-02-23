package ru.sersb.wgsingbox.service

import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.security.crypto.password.PasswordEncoder
import ru.sersb.wgsingbox.model.entity.User
import ru.sersb.wgsingbox.model.enum.UserRole
import ru.sersb.wgsingbox.model.exception.AuthenticationException
import ru.sersb.wgsingbox.model.exception.ValidationException
import ru.sersb.wgsingbox.repository.UserRepository
import ru.sersb.wgsingbox.util.JwtTokenProvider

class AuthServiceTest {

    private lateinit var authService: AuthService
    private lateinit var userRepository: UserRepository
    private lateinit var jwtTokenProvider: JwtTokenProvider
    private lateinit var passwordEncoder: PasswordEncoder

    private val secret = "test-secret-key-for-testing-purposes-only-must-be-at-least-256-bits-long"
    private val expiration = 3600000L
    private val refreshExpiration = 86400000L

    @BeforeEach
    fun setUp() {
        userRepository = mockk()
        jwtTokenProvider = JwtTokenProvider(secret, expiration, refreshExpiration)
        passwordEncoder = mockk()

        authService = AuthService(userRepository, jwtTokenProvider, passwordEncoder)
    }

    @Test
    fun `login should return token when credentials are valid`() {
        // Arrange
        val user = User(
            username = "testuser",
            password = "\$2a\$10\$encoded-password-hash",
            email = "test@example.com",
            role = UserRole.USER
        )
        user.id = 1L

        every { userRepository.findByUsername("testuser") } returns user
        every { passwordEncoder.matches("password", user.password) } returns true

        // Act
        val result = authService.login("testuser", "password")

        // Assert
        assertNotNull(result.token)
        assertTrue(result.token.isNotEmpty())
        assertEquals("testuser", result.user.username)
        assertEquals(UserRole.USER.name, result.user.role)
        verify { userRepository.findByUsername("testuser") }
        verify { passwordEncoder.matches("password", user.password) }
    }

    @Test
    fun `login should throw exception when user not found`() {
        // Arrange
        every { userRepository.findByUsername("nonexistent") } returns null

        // Act & Assert
        assertThrows<AuthenticationException> {
            authService.login("nonexistent", "password")
        }

        verify { userRepository.findByUsername("nonexistent") }
    }

    @Test
    fun `login should throw exception when password is incorrect`() {
        // Arrange
        val user = User(
            username = "testuser",
            password = "\$2a\$10\$encoded-password-hash",
            email = "test@example.com",
            role = UserRole.USER
        )
        user.id = 1L

        every { userRepository.findByUsername("testuser") } returns user
        every { passwordEncoder.matches("wrongpassword", user.password) } returns false

        // Act & Assert
        assertThrows<AuthenticationException> {
            authService.login("testuser", "wrongpassword")
        }

        verify { userRepository.findByUsername("testuser") }
        verify { passwordEncoder.matches("wrongpassword", user.password) }
    }

    @Test
    fun `register should create new user when username is available`() {
        // Arrange
        every { userRepository.existsByUsername("newuser") } returns false
        every { userRepository.existsByEmail("new@example.com") } returns false
        every { passwordEncoder.encode("password") } returns "encoded-password"

        val userSlot = slot<User>()
        every { userRepository.save(capture(userSlot)) } answers { userSlot.captured.apply { id = 2L } }

        // Act
        val result = authService.register("newuser", "password", "new@example.com")

        // Assert
        assertEquals("newuser", result.username)
        assertEquals("new@example.com", result.email)
        assertEquals(UserRole.USER.name, result.role)

        verify { userRepository.existsByUsername("newuser") }
        verify { userRepository.existsByEmail("new@example.com") }
        verify { passwordEncoder.encode("password") }
        verify { userRepository.save(any()) }
    }

    @Test
    fun `register should throw exception when username already exists`() {
        // Arrange
        every { userRepository.existsByUsername("existinguser") } returns true

        // Act & Assert
        assertThrows<ValidationException> {
            authService.register("existinguser", "password", "new@example.com")
        }

        verify { userRepository.existsByUsername("existinguser") }
    }

    @Test
    fun `register should throw exception when email already exists`() {
        // Arrange
        every { userRepository.existsByUsername("newuser") } returns false
        every { userRepository.existsByEmail("existing@example.com") } returns true

        // Act & Assert
        assertThrows<ValidationException> {
            authService.register("newuser", "password", "existing@example.com")
        }

        verify { userRepository.existsByUsername("newuser") }
        verify { userRepository.existsByEmail("existing@example.com") }
    }

    @Test
    fun `refreshToken should return new token when refresh token is valid`() {
        // Arrange
        val user = User(
            username = "testuser",
            password = "password",
            email = "test@example.com",
            role = UserRole.USER
        )
        user.id = 1L

        val refreshToken = jwtTokenProvider.generateRefreshToken(user)
        val userId = jwtTokenProvider.getUserIdFromToken(refreshToken)!!

        every { userRepository.findById(userId) } returns Optional.of(user)

        // Act
        val result = authService.refreshToken(refreshToken)

        // Assert
        assertNotNull(result.token)
        assertTrue(result.token.isNotEmpty())
        assertNotEquals(refreshToken, result.token)

        verify { userRepository.findById(userId) }
    }

    @Test
    fun `refreshToken should throw exception when refresh token is invalid`() {
        // Arrange
        val invalidToken = "invalid.token.here"

        // Act & Assert
        assertThrows<AuthenticationException> {
            authService.refreshToken(invalidToken)
        }
    }

    @Test
    fun `getCurrentUser should return user when token is valid`() {
        // Arrange
        val user = User(
            username = "testuser",
            password = "password",
            email = "test@example.com",
            role = UserRole.USER
        )
        user.id = 1L

        val token = jwtTokenProvider.generateToken(user)
        val userId = jwtTokenProvider.getUserIdFromToken(token)!!

        every { userRepository.findById(userId) } returns Optional.of(user)

        // Act
        val result = authService.getCurrentUser(token)

        // Assert
        assertEquals("testuser", result.username)
        assertEquals(UserRole.USER.name, result.role)

        verify { userRepository.findById(userId) }
    }

    @Test
    fun `changePassword should update password when old password is correct`() {
        // Arrange
        val user = User(
            username = "testuser",
            password = "\$2a\$10\$old-encoded-hash",
            email = "test@example.com",
            role = UserRole.USER
        )
        user.id = 1L

        every { userRepository.findById(1L) } returns Optional.of(user)
        every { passwordEncoder.matches("oldpassword", user.password) } returns true
        every { passwordEncoder.encode("newpassword") } returns "new-encoded-hash"
        every { userRepository.save(any()) } returns user

        // Act
        authService.changePassword(1L, "oldpassword", "newpassword")

        // Assert
        verify { userRepository.findById(1L) }
        verify { passwordEncoder.matches("oldpassword", user.password) }
        verify { passwordEncoder.encode("newpassword") }
        verify { userRepository.save(user) }
    }

    @Test
    fun `changePassword should throw exception when old password is incorrect`() {
        // Arrange
        val user = User(
            username = "testuser",
            password = "\$2a\$10\$old-encoded-hash",
            email = "test@example.com",
            role = UserRole.USER
        )
        user.id = 1L

        every { userRepository.findById(1L) } returns Optional.of(user)
        every { passwordEncoder.matches("wrongpassword", user.password) } returns false

        // Act & Assert
        assertThrows<ValidationException> {
            authService.changePassword(1L, "wrongpassword", "newpassword")
        }

        verify { userRepository.findById(1L) }
        verify { passwordEncoder.matches("wrongpassword", user.password) }
        verify(exactly = 0) { userRepository.save(any()) }
    }
}
