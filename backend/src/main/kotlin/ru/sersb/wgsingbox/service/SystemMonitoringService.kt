package ru.sersb.wgsingbox.service

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import ru.sersb.wgsingbox.integration.singbox.SingBoxManager
import ru.sersb.wgsingbox.integration.wireguard.WireGuardManager
import ru.sersb.wgsingbox.model.dto.response.SystemStatusResponse
import ru.sersb.wgsingbox.websocket.StatusWebSocketHandler
import java.lang.management.ManagementFactory
import java.net.InetAddress
import java.time.LocalDateTime

@Service
class SystemMonitoringService(
    private val wireGuardManager: WireGuardManager,
    private val singBoxManager: SingBoxManager,
    private val statusWebSocketHandler: StatusWebSocketHandler
) {
    private val logger = KotlinLogging.logger {}
    private val startTime = System.currentTimeMillis()

    @Scheduled(fixedRate = 5000) // Every 5 seconds
    fun broadcastStatus() {
        try {
            statusWebSocketHandler.broadcastStatus()
        } catch (e: Exception) {
            logger.error(e) { "Failed to broadcast status" }
        }
    }

    fun getStatus(): SystemStatusResponse {
        val wgStatus = runBlocking { wireGuardManager.getStatus() }
        val sbStatus = singBoxManager.getStatus()

        return SystemStatusResponse(
            wireGuardStatus = wgStatus,
            singBoxStatus = sbStatus,
            connectedPeers = 0,
            totalTransferRx = 0,
            totalTransferTx = 0,
            uptime = System.currentTimeMillis() - startTime,
            cpuUsage = getCpuUsage(),
            memoryUsage = getMemoryUsage(),
            diskUsage = getDiskUsage(),
            timestamp = LocalDateTime.now().toString()
        )
    }

    fun getSystemInfo(): ru.sersb.wgsingbox.model.dto.response.SystemInfoResponse {
        val os = System.getProperty("os.name")
        val osVersion = System.getProperty("os.version")
        val arch = System.getProperty("os.arch")

        return ru.sersb.wgsingbox.model.dto.response.SystemInfoResponse(
            hostname = InetAddress.getLocalHost().hostName,
            os = os,
            osVersion = osVersion,
            kernelVersion = osVersion,
            arch = arch,
            uptime = System.currentTimeMillis() - startTime,
            cpuCores = Runtime.getRuntime().availableProcessors(),
            totalMemory = Runtime.getRuntime().totalMemory(),
            totalDisk = getTotalDiskSpace()
        )
    }

    fun getWebSocketStats(): Map<String, Any> {
        return mapOf(
            "statusClients" to statusWebSocketHandler.getSessionCount(),
            "logClients" to 0 // LogWebSocketHandler sessions not tracked
        )
    }

    private fun getCpuUsage(): Double? {
        return try {
            val bean = ManagementFactory.getOperatingSystemMXBean()
            if (bean is com.sun.management.OperatingSystemMXBean) {
                bean.processCpuLoad * 100
            } else {
                null
            }
        } catch (e: Exception) {
            logger.debug(e) { "Failed to get CPU usage" }
            null
        }
    }

    private fun getMemoryUsage(): Double? {
        return try {
            val runtime = Runtime.getRuntime()
            val usedMemory = runtime.totalMemory() - runtime.freeMemory()
            val maxMemory = runtime.maxMemory()
            (usedMemory.toDouble() / maxMemory) * 100
        } catch (e: Exception) {
            logger.debug(e) { "Failed to get memory usage" }
            null
        }
    }

    private fun getDiskUsage(): Double? {
        return try {
            val root = java.nio.file.FileSystems.getDefault().getRootDirectories().first()
            val store = java.nio.file.Files.getFileStore(root)
            val used = store.usableSpace
            val total = store.totalSpace
            ((total - used).toDouble() / total) * 100
        } catch (e: Exception) {
            logger.debug(e) { "Failed to get disk usage" }
            null
        }
    }

    private fun getTotalDiskSpace(): Long {
        return try {
            val root = java.nio.file.FileSystems.getDefault().getRootDirectories().first()
            val store = java.nio.file.Files.getFileStore(root)
            store.totalSpace
        } catch (e: Exception) {
            logger.debug(e) { "Failed to get total disk space" }
            0L
        }
    }
}
