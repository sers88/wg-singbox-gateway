package ru.sersb.wgsingbox.model.dto.response

import ru.sersb.wgsingbox.model.entity.ConfigBackup

data class ConfigBackupResponse(
    val id: Long?,
    val name: String,
    val description: String?,
    val createdAt: String
) {
    companion object {
        fun fromEntity(entity: ConfigBackup): ConfigBackupResponse {
            return ConfigBackupResponse(
                id = entity.id,
                name = entity.name,
                description = entity.description,
                createdAt = entity.createdAt.toString()
            )
        }
    }
}
