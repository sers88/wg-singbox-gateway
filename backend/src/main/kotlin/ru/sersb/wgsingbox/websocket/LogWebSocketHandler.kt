package ru.sersb.wgsingbox.websocket

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.io.BufferedReader
import java.io.FileReader
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

@Component
class LogWebSocketHandler(
    private val logFilePath: String = "./logs/application.log"
) : TextWebSocketHandler() {

    private val logger = KotlinLogging.logger {}
    private val sessions = ConcurrentHashMap<String, WebSocketSession>()
    private val executor = Executors.newSingleThreadScheduledExecutor()
    private var tailTask: ScheduledFuture<*>? = null

    init {
        startLogTailing()
    }

    override fun afterConnectionEstablished(session: WebSocketSession) {
        sessions[session.id] = session
        logger.info { "Log WebSocket client connected: ${session.id}" }

        // Send recent logs
        sendRecentLogs(session)
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        sessions.remove(session.id)
        logger.info { "Log WebSocket client disconnected: ${session.id}" }

        if (sessions.isEmpty()) {
            stopLogTailing()
        }
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        logger.debug { "Received message from ${session.id}: ${message.payload}" }
    }

    override fun handleTransportError(session: WebSocketSession, exception: Throwable) {
        logger.error(exception) { "WebSocket error for ${session.id}" }
        sessions.remove(session.id)

        if (sessions.isEmpty()) {
            stopLogTailing()
        }
    }

    private fun startLogTailing() {
        if (tailTask != null) return

        tailTask = executor.scheduleAtFixedRate({
            try {
                broadcastNewLogs()
            } catch (e: Exception) {
                logger.error(e) { "Error in log tailing" }
            }
        }, 1, 1, TimeUnit.SECONDS)

        logger.info { "Log tailing started" }
    }

    private fun stopLogTailing() {
        tailTask?.cancel(false)
        tailTask = null
        logger.info { "Log tailing stopped" }
    }

    private fun sendRecentLogs(session: WebSocketSession) {
        try {
            val logs = readRecentLogs(100)
            val message = buildJsonLog(logs, "initial")

            if (session.isOpen) {
                session.sendMessage(TextMessage(message))
            }
        } catch (e: Exception) {
            logger.error(e) { "Failed to send recent logs" }
        }
    }

    private fun broadcastNewLogs() {
        val logs = readRecentLogs(10) // Last 10 lines
        val message = buildJsonLog(logs, "update")

        sessions.values.forEach { session ->
            try {
                if (session.isOpen) {
                    session.sendMessage(TextMessage(message))
                }
            } catch (e: Exception) {
                logger.error(e) { "Failed to send logs to ${session.id}" }
                sessions.remove(session.id)
            }
        }
    }

    private fun readRecentLogs(lines: Int): List<String> {
        return try {
            val file = java.io.File(logFilePath)
            if (!file.exists()) return emptyList()

            val allLines = file.readLines()
            allLines.takeLast(lines)
        } catch (e: Exception) {
            logger.error(e) { "Failed to read log file" }
            emptyList()
        }
    }

    private fun buildJsonLog(logs: List<String>, type: String): String {
        return """{
            "type": "$type",
            "logs": ${logs.map { "\"$it\"" }.joinToString(",", "[", "]")}
        }""".trimIndent()
    }

    fun getSessionCount(): Int = sessions.size
}
