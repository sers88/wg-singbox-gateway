package ru.sersb.wgsingbox.model.dto.response

import ru.sersb.wgsingbox.model.enum.ServiceStatus

data class WireGuardStatusResponse(
    val status: ServiceStatus,
    val interfaceName: String,
    val publicKey: String,
    val listenPort: Int,
    val peers: List<PeerStats>,
    val uptime: Long?
)

data class PeerStats(
    val publicKey: String,
    val endpoint: String?,
    val allowedIps: String,
    val latestHandshake: String?,
    val transferRx: Long,
    val transferTx: Long
)
