package ru.sersb.wgsingbox.model.dto.response

data class UserResponse(
    val id: Long?,
    val username: String,
    val email: String?,
    val role: String,
    val lastLogin: String?
) {
    companion object {
        fun fromEntity(user: ru.sersb.wgsingbox.model.entity.User): UserResponse {
            return UserResponse(
                id = user.id,
                username = user.username,
                email = user.email,
                role = user.role.name,
                lastLogin = user.lastLogin?.toString()
            )
        }
    }
}
