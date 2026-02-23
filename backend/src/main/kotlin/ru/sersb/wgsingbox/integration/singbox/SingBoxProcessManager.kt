package ru.sersb.wgsingbox.integration.singbox

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.io.BufferedReader
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.TimeUnit

@Component
class SingBoxProcessManager(
    @Value("\${singbox.executable:/usr/local/bin/sing-box}")
    private val singboxExecutable: String
) {
    private val logger = KotlinLogging.logger {}
    private var currentProcess: Process? = null
    private var startTime: Long = 0

    fun start(configPath: String): Process {
        stop() // Ensure no running instance

        val processBuilder = ProcessBuilder(
            singboxExecutable,
            "run",
            "-c", configPath
        ).redirectErrorStream(true)

        val process = processBuilder.start()
        startTime = System.currentTimeMillis()

        // Start log reader in background
        Thread {
            process.inputStream.bufferedReader().use { reader ->
                reader.lines().forEach { line ->
                    logger.info { "SingBox: $line" }
                }
            }
        }.start()

        currentProcess = process
        logger.info { "SingBox process started with PID: ${process.pid()}" }

        return process
    }

    fun stop(): Boolean {
        currentProcess?.let { process ->
            if (process.isAlive) {
                logger.info { "Stopping SingBox process (PID: ${process.pid()})..." }

                process.destroy()
                if (!process.waitFor(5, TimeUnit.SECONDS)) {
                    logger.warn { "SingBox did not stop gracefully, forcing..." }
                    process.destroyForcibly()
                    process.waitFor(5, TimeUnit.SECONDS)
                }

                logger.info { "SingBox process stopped" }
            }
        }

        currentProcess = null
        startTime = 0
        return true
    }

    fun isRunning(): Boolean {
        return currentProcess?.isAlive == true
    }

    fun getUptime(): Long {
        return if (isRunning()) {
            System.currentTimeMillis() - startTime
        } else {
            0
        }
    }

    fun getPid(): Long? {
        return currentProcess?.pid()
    }

    suspend fun writeConfig(configPath: String, content: String): Result<Unit> {
        return try {
            val path = Path.of(configPath)
            val parent = path.parent
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent)
            }

            Files.write(path, content.toByteArray(Charsets.UTF_8))
            logger.info { "SingBox config written to $configPath" }
            Result.success(Unit)
        } catch (e: Exception) {
            logger.error(e) { "Failed to write SingBox config: ${e.message}" }
            Result.failure(e)
        }
    }

    suspend fun validateConfig(configPath: String): Result<Unit> {
        return try {
            val processBuilder = ProcessBuilder(
                singboxExecutable,
                "check",
                "-c", configPath
            ).redirectErrorStream(true)

            val process = processBuilder.start()
            val completed = process.waitFor(30, TimeUnit.SECONDS)

            if (completed && process.exitValue() == 0) {
                logger.info { "SingBox config is valid" }
                Result.success(Unit)
            } else {
                val output = process.inputStream.bufferedReader().use(BufferedReader::readText)
                val errorMsg = if (completed) {
                    "Config validation failed (exit code ${process.exitValue()}): $output"
                } else {
                    "Config validation timed out"
                }
                logger.error { errorMsg }
                Result.failure(RuntimeException(errorMsg))
            }
        } catch (e: Exception) {
            logger.error(e) { "Failed to validate SingBox config: ${e.message}" }
            Result.failure(e)
        }
    }
}
