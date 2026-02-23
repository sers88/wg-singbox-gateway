package ru.sersb.wgsingbox.integration.wireguard

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.io.BufferedReader
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.PosixFilePermission
import java.util.concurrent.TimeUnit

@Component
class WireGuardCommandExecutor(
    @Value("\${wireguard.executable:/usr/bin/wg}")
    private val wgExecutable: String,

    @Value("\${wireguard.wg-quick:/usr/bin/wg-quick}")
    private val wgQuick: String,

    @Value("\${wireguard.config-path:/etc/wireguard}")
    private val configPath: String
) {
    private val logger = KotlinLogging.logger {}

    suspend fun executeWgQuick(action: String, interfaceName: String): Result<String> {
        return runCommand(
            command = listOf(wgQuick, action, interfaceName),
            timeoutSeconds = 30
        )
    }

    suspend fun getWgShow(): WireGuardStatus {
        val result = runCommand(listOf(wgExecutable, "show"), 10)

        if (result.isFailure) {
            return WireGuardStatus(
                interfaceName = "unknown",
                publicKey = "",
                listenPort = 0,
                peers = emptyList()
            )
        }

        return parseWgShowOutput(result.getOrNull() ?: "")
    }

    suspend fun getWgShowPeer(publicKey: String): PeerStats? {
        val result = runCommand(listOf(wgExecutable, "show", publicKey), 10)
        if (result.isFailure) return null

        return parseWgShowPeerOutput(result.getOrNull() ?: "")
    }

    suspend fun writeConfig(filename: String, content: String): Result<Unit> {
        return try {
            val dirPath = Path.of(configPath)
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath)
            }

            val filePath = dirPath.resolve(filename)

            Files.write(filePath, content.toByteArray(Charsets.UTF_8))

            // Set restrictive permissions
            try {
                val permissions = setOf(
                    PosixFilePermission.OWNER_READ,
                    PosixFilePermission.OWNER_WRITE
                )
                Files.setPosixFilePermissions(filePath, permissions)
            } catch (e: UnsupportedOperationException) {
                // Windows doesn't support POSIX permissions
                logger.warn { "Cannot set POSIX permissions on Windows" }
            }

            logger.info { "Written WireGuard config to $filePath" }
            Result.success(Unit)
        } catch (e: Exception) {
            logger.error(e) { "Failed to write config: ${e.message}" }
            Result.failure(e)
        }
    }

    suspend fun deleteConfig(filename: String): Result<Unit> {
        return try {
            val filePath = Path.of(configPath, filename)
            if (Files.exists(filePath)) {
                Files.delete(filePath)
                logger.info { "Deleted WireGuard config: $filePath" }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            logger.error(e) { "Failed to delete config: ${e.message}" }
            Result.failure(e)
        }
    }

    suspend fun checkInterfaceExists(interfaceName: String): Boolean {
        val path = Path.of("/sys/class/net", interfaceName)
        return Files.exists(path)
    }

    private suspend fun runCommand(
        command: List<String>,
        timeoutSeconds: Long
    ): Result<String> {
        return try {
            val processBuilder = ProcessBuilder(command)
                .redirectErrorStream(true)

            val process = processBuilder.start()
            val completed = process.waitFor(timeoutSeconds, TimeUnit.SECONDS)

            val output = process.inputStream.bufferedReader().use(BufferedReader::readText)

            if (completed) {
                if (process.exitValue() == 0) {
                    Result.success(output)
                } else {
                    logger.error { "Command failed with exit code ${process.exitValue()}: $output" }
                    Result.failure(RuntimeException("Command failed with exit code ${process.exitValue()}"))
                }
            } else {
                process.destroy()
                Result.failure(RuntimeException("Command timed out after $timeoutSeconds seconds"))
            }
        } catch (e: Exception) {
            logger.error(e) { "Failed to execute command: ${e.message}" }
            Result.failure(e)
        }
    }

    private fun parseWgShowOutput(output: String): WireGuardStatus {
        val lines = output.lines()
        var publicKey = ""
        var listenPort = 0
        val peers = mutableListOf<PeerStats>()
        var currentPeer: PeerStats? = null

        for (line in lines) {
            val trimmed = line.trim()
            when {
                trimmed.startsWith("public key:") -> {
                    publicKey = trimmed.substringAfter("public key:").trim()
                }
                trimmed.startsWith("listening port:") -> {
                    listenPort = trimmed.substringAfter("listening port:").trim().toIntOrNull() ?: 0
                }
                trimmed.startsWith("peer:") -> {
                    currentPeer?.let { peers.add(it) }
                    val peerPubKey = trimmed.substringAfter("peer:").trim()
                    currentPeer = PeerStats(
                        publicKey = peerPubKey,
                        endpoint = null,
                        allowedIps = "",
                        latestHandshake = null,
                        transferRx = 0,
                        transferTx = 0
                    )
                }
                trimmed.startsWith("endpoint:") && currentPeer != null -> {
                    currentPeer = currentPeer!!.copy(endpoint = trimmed.substringAfter("endpoint:").trim())
                }
                trimmed.startsWith("allowed ips:") && currentPeer != null -> {
                    currentPeer = currentPeer!!.copy(allowedIps = trimmed.substringAfter("allowed ips:").trim())
                }
                trimmed.startsWith("latest handshake:") && currentPeer != null -> {
                    currentPeer = currentPeer!!.copy(latestHandshake = trimmed.substringAfter("latest handshake:").trim())
                }
                trimmed.startsWith("transfer:") && currentPeer != null -> {
                    val transfer = trimmed.substringAfter("transfer:").trim()
                    val (rx, tx) = transfer.split(",").map { it.trim().split(" ")[0].toLongOrNull() ?: 0L }
                    currentPeer = currentPeer!!.copy(transferRx = rx, transferTx = tx)
                }
            }
        }

        currentPeer?.let { peers.add(it) }

        return WireGuardStatus(
            interfaceName = "wg0",
            publicKey = publicKey,
            listenPort = listenPort,
            peers = peers
        )
    }

    private fun parseWgShowPeerOutput(output: String): PeerStats? {
        // Simplified parsing for peer-specific output
        return parseWgShowOutput(output).peers.firstOrNull()
    }
}

data class WireGuardStatus(
    val interfaceName: String,
    val publicKey: String,
    val listenPort: Int,
    val peers: List<PeerStats>
)

data class PeerStats(
    val publicKey: String,
    val endpoint: String?,
    val allowedIps: String,
    val latestHandshake: String?,
    val transferRx: Long,
    val transferTx: Long
)
