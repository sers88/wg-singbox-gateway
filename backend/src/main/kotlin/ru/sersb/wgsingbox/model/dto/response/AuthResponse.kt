package ru.sersb.wgsingbox.model.dto.response

data class AuthResponse(
    val token: String,
    val refreshToken: String? = null,
    val user: UserResponse
)
