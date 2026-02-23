package ru.sersb.wgsingbox.model.dto.response

import ru.sersb.wgsingbox.model.enum.ServiceStatus

data class SystemStatusResponse(
    val wireGuardStatus: ServiceStatus,
    val singBoxStatus: ServiceStatus,
    val connectedPeers: Int,
    val totalTransferRx: Long,
    val totalTransferTx: Long,
    val uptime: Long,
    val cpuUsage: Double?,
    val memoryUsage: Double?,
    val diskUsage: Double?,
    val timestamp: String
)
