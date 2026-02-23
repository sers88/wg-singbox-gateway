package ru.sersb.wgsingbox.service

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import ru.sersb.wgsingbox.model.dto.request.ChangePasswordRequest
import ru.sersb.wgsingbox.model.dto.request.CreateUserRequest
import ru.sersb.wgsingbox.model.dto.request.LoginRequest
import ru.sersb.wgsingbox.model.dto.response.ApiResponse
import ru.sersb.wgsingbox.model.dto.response.AuthResponse
import ru.sersb.wgsingbox.model.dto.response.UserResponse
import ru.sersb.wgsingbox.model.exception.AuthenticationException
import ru.sersb.wgsingbox.model.exception.ValidationException
import ru.sersb.wgsingbox.model.entity.User
import ru.sersb.wgsingbox.repository.UserRepository
import ru.sersb.wgsingbox.util.JwtTokenProvider
import java.time.LocalDateTime

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenProvider: JwtTokenProvider
) {
    private val logger = KotlinLogging.logger {}

    fun login(request: LoginRequest): AuthResponse {
        val user = userRepository.findByUsername(request.username)
            ?: throw AuthenticationException("Invalid credentials")

        if (!user.enabled) {
            throw AuthenticationException("Account is disabled")
        }

        if (!passwordEncoder.matches(request.password, user.password)) {
            throw AuthenticationException("Invalid credentials")
        }

        user.lastLogin = LocalDateTime.now()
        userRepository.save(user)

        val token = jwtTokenProvider.generateToken(user)
        val refreshToken = jwtTokenProvider.generateRefreshToken(user)

        logger.info { "User ${user.username} logged in successfully" }

        return AuthResponse(
            token = token,
            refreshToken = refreshToken,
            user = UserResponse.fromEntity(user)
        )
    }

    fun changePassword(userId: Long, request: ChangePasswordRequest): UserResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { ru.sersb.wgsingbox.model.exception.ResourceNotFoundException("User not found") }

        if (!passwordEncoder.matches(request.currentPassword, user.password)) {
            throw ValidationException("Current password is incorrect")
        }

        user.password = passwordEncoder.encode(request.newPassword)
        user.updatedAt = LocalDateTime.now()
        userRepository.save(user)

        logger.info { "User ${user.username} changed password" }

        return UserResponse.fromEntity(user)
    }

    fun getCurrentUser(userId: Long): UserResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { ru.sersb.wgsingbox.model.exception.ResourceNotFoundException("User not found") }

        return UserResponse.fromEntity(user)
    }

    fun createUser(request: CreateUserRequest, currentUser: User?): UserResponse {
        if (userRepository.existsByUsername(request.username)) {
            throw ValidationException("Username already exists", listOf("Username ${request.username} is already taken"))
        }

        if (request.email != null && userRepository.existsByEmail(request.email)) {
            throw ValidationException("Email already exists", listOf("Email ${request.email} is already registered"))
        }

        val user = User(
            username = request.username,
            password = passwordEncoder.encode(request.password),
            email = request.email,
            role = request.role,
            enabled = true
        )

        val savedUser = userRepository.save(user)

        logger.info { "User ${savedUser.username} created by ${currentUser?.username}" }

        return UserResponse.fromEntity(savedUser)
    }

    fun deleteUser(userId: Long, currentUser: User) {
        if (currentUser.id == userId) {
            throw ValidationException("Cannot delete your own account")
        }

        val user = userRepository.findById(userId)
            .orElseThrow { ru.sersb.wgsingbox.model.exception.ResourceNotFoundException("User not found") }

        userRepository.delete(user)

        logger.info { "User ${user.username} deleted by ${currentUser.username}" }
    }

    fun toggleUser(userId: Long, currentUser: User): UserResponse {
        if (currentUser.id == userId) {
            throw ValidationException("Cannot disable your own account")
        }

        val user = userRepository.findById(userId)
            .orElseThrow { ru.sersb.wgsingbox.model.exception.ResourceNotFoundException("User not found") }

        user.enabled = !user.enabled
        user.updatedAt = LocalDateTime.now()
        val savedUser = userRepository.save(user)

        logger.info { "User ${user.username} ${if (savedUser.enabled) "enabled" else "disabled"} by ${currentUser.username}" }

        return UserResponse.fromEntity(savedUser)
    }
}

object UserResponseMapper {
    fun fromEntity(user: User): UserResponse {
        return UserResponse(
            id = user.id,
            username = user.username,
            email = user.email,
            role = user.role.name,
            lastLogin = user.lastLogin?.toString()
        )
    }
}
