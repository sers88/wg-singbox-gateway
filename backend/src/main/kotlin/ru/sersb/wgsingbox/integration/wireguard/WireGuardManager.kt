package ru.sersb.wgsingbox.integration.wireguard

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import ru.sersb.wgsingbox.model.entity.Peer
import ru.sersb.wgsingbox.model.entity.WireGuardConfig
import ru.sersb.wgsingbox.model.enum.ServiceStatus

@Component
class WireGuardManager(
    private val commandExecutor: WireGuardCommandExecutor,
    private val configGenerator: WireGuardConfigGenerator,

    @Value("\${wireguard.interface-name:wg0}")
    private val interfaceName: String
) {
    private val logger = KotlinLogging.logger {}
    private var currentStatus: ServiceStatus = ServiceStatus.STOPPED

    suspend fun setupInterface(config: WireGuardConfig, peers: List<Peer>): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val wgConfig = configGenerator.generateConfig(config, peers)

                commandExecutor.writeConfig("$interfaceName.conf", wgConfig)
                    .getOrThrow()

                logger.info { "WireGuard interface $interfaceName configured successfully" }
                Result.success(Unit)
            } catch (e: Exception) {
                logger.error(e) { "Failed to setup WireGuard interface: ${e.message}" }
                Result.failure(e)
            }
        }
    }

    suspend fun startInterface(): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                // Check if interface already exists and is running
                if (commandExecutor.checkInterfaceExists(interfaceName)) {
                    logger.info { "Interface $interfaceName already exists, stopping first..." }
                    stopInterface()
                }

                val result = commandExecutor.executeWgQuick("up", interfaceName)
                if (result.isSuccess) {
                    currentStatus = ServiceStatus.RUNNING
                    logger.info { "WireGuard interface $interfaceName started successfully" }
                    Result.success(Unit)
                } else {
                    currentStatus = ServiceStatus.ERROR
                    Result.failure(result.exceptionOrNull() ?: RuntimeException("Failed to start interface"))
                }
            } catch (e: Exception) {
                currentStatus = ServiceStatus.ERROR
                logger.error(e) { "Failed to start WireGuard interface: ${e.message}" }
                Result.failure(e)
            }
        }
    }

    suspend fun stopInterface(): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val result = commandExecutor.executeWgQuick("down", interfaceName)
                if (result.isSuccess) {
                    currentStatus = ServiceStatus.STOPPED
                    logger.info { "WireGuard interface $interfaceName stopped successfully" }
                    Result.success(Unit)
                } else {
                    // Even if down command fails, consider it stopped
                    currentStatus = ServiceStatus.STOPPED
                    Result.success(Unit)
                }
            } catch (e: Exception) {
                currentStatus = ServiceStatus.STOPPED
                logger.warn(e) { "Failed to stop WireGuard interface (may be already stopped): ${e.message}" }
                Result.success(Unit)
            }
        }
    }

    suspend fun restartInterface(): Result<Unit> {
        return stopInterface().flatMap { startInterface() }
    }

    suspend fun getStatus(): ServiceStatus {
        val exists = commandExecutor.checkInterfaceExists(interfaceName)
        return if (exists && currentStatus == ServiceStatus.RUNNING) {
            ServiceStatus.RUNNING
        } else if (exists) {
            ServiceStatus.STOPPED
        } else {
            ServiceStatus.STOPPED
        }
    }

    suspend fun getDetailedStatus(): Pair<ServiceStatus, WireGuardStatus?> {
        return try {
            val status = getStatus()
            val wgStatus = commandExecutor.getWgShow()
            status to wgStatus
        } catch (e: Exception) {
            logger.error(e) { "Failed to get detailed status: ${e.message}" }
            ServiceStatus.ERROR to null
        }
    }

    suspend fun updatePeerStats(publicKey: String, stats: ru.sersb.wgsingbox.integration.wireguard.PeerStats) {
        // This would update peer stats in database
        logger.debug { "Updating stats for peer $publicKey" }
    }

    fun getCurrentStatus(): ServiceStatus = currentStatus
}
