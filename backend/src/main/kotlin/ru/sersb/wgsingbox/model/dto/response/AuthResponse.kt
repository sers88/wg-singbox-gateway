package ru.sersb.wgsingbox.model.dto.response

data class AuthResponse(
    val token: String,
    val refreshToken: String? = null,
    val user: UserResponse
)

data class UserResponse(
    val id: Long?,
    val username: String,
    val email: String?,
    val role: String,
    val lastLogin: String?
)
