package ru.sersb.wgsingbox.integration.singbox

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import ru.sersb.wgsingbox.model.entity.ProxyConfig
import ru.sersb.wgsingbox.model.entity.RoutingRule
import ru.sersb.wgsingbox.model.enum.ServiceStatus

@Component
class SingBoxManager(
    private val processManager: SingBoxProcessManager,
    private val configGenerator: SingBoxConfigGenerator,

    @Value("\${singbox.config-path:/etc/singbox/config.json}")
    private val configPath: String
) {
    private val logger = KotlinLogging.logger {}
    private var currentStatus: ServiceStatus = ServiceStatus.STOPPED

    suspend fun generateAndWriteConfig(
        proxyConfig: ProxyConfig?,
        rules: List<RoutingRule>,
        customTemplate: String? = null
    ): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val config = configGenerator.generateConfig(proxyConfig, rules, customTemplate)

                // Validate first
                configGenerator.validateConfig(config).getOrThrow()

                // Write to file
                processManager.writeConfig(configPath, config).getOrThrow()

                logger.info { "SingBox config generated and written successfully" }
                Result.success(Unit)
            } catch (e: Exception) {
                logger.error(e) { "Failed to generate SingBox config: ${e.message}" }
                Result.failure(e)
            }
        }
    }

    suspend fun start(proxyConfig: ProxyConfig?, rules: List<RoutingRule>): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                // Stop if running
                stop()

                // Generate and write config
                generateAndWriteConfig(proxyConfig, rules).getOrThrow()

                // Start process
                processManager.start(configPath)
                currentStatus = ServiceStatus.RUNNING

                logger.info { "SingBox started successfully" }
                Result.success(Unit)
            } catch (e: Exception) {
                currentStatus = ServiceStatus.ERROR
                logger.error(e) { "Failed to start SingBox: ${e.message}" }
                Result.failure(e)
            }
        }
    }

    suspend fun stop(): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                processManager.stop()
                currentStatus = ServiceStatus.STOPPED
                logger.info { "SingBox stopped successfully" }
                Result.success(Unit)
            } catch (e: Exception) {
                currentStatus = ServiceStatus.ERROR
                logger.error(e) { "Failed to stop SingBox: ${e.message}" }
                Result.failure(e)
            }
        }
    }

    suspend fun restart(proxyConfig: ProxyConfig?, rules: List<RoutingRule>): Result<Unit> {
        return stop().flatMap { start(proxyConfig, rules) }
    }

    fun getStatus(): ServiceStatus {
        return if (processManager.isRunning && currentStatus == ServiceStatus.RUNNING) {
            ServiceStatus.RUNNING
        } else if (processManager.isRunning) {
            ServiceStatus.RUNNING
        } else {
            ServiceStatus.STOPPED
        }
    }

    fun getUptime(): Long = processManager.uptime
    fun getPid(): Long? = processManager.pid
}
