package ru.sersb.wgsingbox.model.dto.response

import ru.sersb.wgsingbox.model.enum.ServiceStatus

data class WireGuardConfigResponse(
    val id: Long?,
    val privateKey: String,
    val publicKey: String,
    val listenPort: Int,
    val address: String,
    val mtu: Int,
    val postUp: String?,
    val postDown: String?,
    val enabled: Boolean,
    val interfaceName: String,
    val status: ServiceStatus,
    val createdAt: String,
    val updatedAt: String
)
