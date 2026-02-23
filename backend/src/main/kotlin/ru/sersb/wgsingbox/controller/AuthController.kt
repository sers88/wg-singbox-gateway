package ru.sersb.wgsingbox.controller

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import ru.sersb.wgsingbox.model.dto.request.ChangePasswordRequest
import ru.sersb.wgsingbox.model.dto.request.CreateUserRequest
import ru.sersb.wgsingbox.model.dto.request.LoginRequest
import ru.sersb.wgsingbox.model.dto.response.ApiResponse
import ru.sersb.wgsingbox.model.dto.response.AuthResponse
import ru.sersb.wgsingbox.model.dto.response.UserResponse
import ru.sersb.wgsingbox.model.exception.ResourceNotFoundException
import ru.sersb.wgsingbox.model.exception.ValidationException
import ru.sersb.wgsingbox.model.exception.AuthenticationException
import ru.sersb.wgsingbox.service.AuthService
import ru.sersb.wgsingbox.util.UserPrincipal

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService
) {
    private val logger = KotlinLogging.logger {}

    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<ApiResponse<AuthResponse>> {
        val response = authService.login(request)
        return ResponseEntity.ok(ApiResponse.success(response))
    }

    @PostMapping("/logout")
    fun logout(@AuthenticationPrincipal principal: UserPrincipal): ResponseEntity<ApiResponse<Void>> {
        logger.info { "User ${principal.username} logged out" }
        return ResponseEntity.ok(ApiResponse.success(null, "Logged out successfully"))
    }

    @GetMapping("/me")
    fun getCurrentUser(@AuthenticationPrincipal principal: UserPrincipal): ResponseEntity<ApiResponse<UserResponse>> {
        val user = authService.getCurrentUser(principal.id!!)
        return ResponseEntity.ok(ApiResponse.success(user))
    }

    @PostMapping("/change-password")
    fun changePassword(
        @Valid @RequestBody request: ChangePasswordRequest,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<ApiResponse<UserResponse>> {
        val user = authService.changePassword(principal.id!!, request)
        return ResponseEntity.ok(ApiResponse.success(user, "Password changed successfully"))
    }

    @PostMapping("/users")
    fun createUser(
        @Valid @RequestBody request: CreateUserRequest,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<ApiResponse<UserResponse>> {
        val currentUser = principal.id?.let { authService.getCurrentUser(it) }
        val user = authService.createUser(request, ru.sersb.wgsingbox.model.entity.User().copy(id = principal.id))
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(user, "User created successfully"))
    }

    @DeleteMapping("/users/{id}")
    fun deleteUser(
        @PathVariable id: Long,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<ApiResponse<Void>> {
        authService.deleteUser(id, ru.sersb.wgsingbox.model.entity.User().copy(id = principal.id))
        return ResponseEntity.ok(ApiResponse.success(null, "User deleted successfully"))
    }

    @PostMapping("/users/{id}/toggle")
    fun toggleUser(
        @PathVariable id: Long,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<ApiResponse<UserResponse>> {
        val user = authService.toggleUser(id, ru.sersb.wgsingbox.model.entity.User().copy(id = principal.id))
        return ResponseEntity.ok(ApiResponse.success(user))
    }
}

@RestControllerAdvice
class AuthControllerAdvice {
    private val logger = KotlinLogging.logger {}

    @ExceptionHandler(ValidationException::class)
    fun handleValidationException(ex: ValidationException): ResponseEntity<ApiResponse<Void>> {
        logger.warn { "Validation error: ${ex.message}" }
        return ResponseEntity.badRequest().body(ApiResponse.error(ex.message ?: "Validation failed", ex.errors))
    }

    @ExceptionHandler(AuthenticationException::class)
    fun handleAuthenticationException(ex: AuthenticationException): ResponseEntity<ApiResponse<Void>> {
        logger.warn { "Authentication error: ${ex.message}" }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error(ex.message ?: "Authentication failed"))
    }

    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleResourceNotFoundException(ex: ResourceNotFoundException): ResponseEntity<ApiResponse<Void>> {
        logger.warn { "Resource not found: ${ex.message}" }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(ex.message ?: "Resource not found"))
    }
}
