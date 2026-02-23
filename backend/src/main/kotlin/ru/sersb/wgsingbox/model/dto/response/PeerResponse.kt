package ru.sersb.wgsingbox.model.dto.response

import java.time.LocalDateTime

data class PeerResponse(
    val id: Long?,
    val publicKey: String,
    val presharedKey: String?,
    val allowedIps: String,
    val name: String,
    val deviceType: String?,
    val endpointIp: String?,
    val endpointPort: Int?,
    val persistentKeepalive: Int,
    val lastHandshake: String?,
    val transferRx: Long,
    val transferTx: Long,
    val enabled: Boolean,
    val createdAt: String,
    val updatedAt: String
) {
    companion object {
        fun fromEntity(entity: ru.sersb.wgsingbox.model.entity.Peer): PeerResponse {
            return PeerResponse(
                id = entity.id,
                publicKey = entity.publicKey,
                presharedKey = entity.presharedKey,
                allowedIps = entity.allowedIps,
                name = entity.name,
                deviceType = entity.deviceType,
                endpointIp = entity.endpointIp,
                endpointPort = entity.endpointPort,
                persistentKeepalive = entity.persistentKeepalive,
                lastHandshake = entity.lastHandshake?.toString(),
                transferRx = entity.transferRx,
                transferTx = entity.transferTx,
                enabled = entity.enabled,
                createdAt = entity.createdAt.toString(),
                updatedAt = entity.updatedAt.toString()
            )
        }
    }
}
