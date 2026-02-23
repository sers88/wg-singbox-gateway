package ru.sersb.wgsingbox.websocket

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import ru.sersb.wgsingbox.integration.singbox.SingBoxManager
import ru.sersb.wgsingbox.integration.wireguard.WireGuardManager
import ru.sersb.wgsingbox.model.dto.response.SystemStatusResponse
import ru.sersb.wgsingbox.model.enum.ServiceStatus
import java.net.InetAddress
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap

@Component
class StatusWebSocketHandler(
    private val wireGuardManager: WireGuardManager,
    private val singBoxManager: SingBoxManager,
    private val objectMapper: ObjectMapper
) : TextWebSocketHandler() {

    private val logger = KotlinLogging.logger {}
    private val sessions = ConcurrentHashMap<String, WebSocketSession>()

    override fun afterConnectionEstablished(session: WebSocketSession) {
        sessions[session.id] = session
        logger.info { "WebSocket client connected: ${session.id}" }

        // Send initial status
        sendInitialStatus(session)
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        sessions.remove(session.id)
        logger.info { "WebSocket client disconnected: ${session.id}" }
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        // Handle client messages if needed
        logger.debug { "Received message from ${session.id}: ${message.payload}" }
    }

    override fun handleTransportError(session: WebSocketSession, exception: Throwable) {
        logger.error(exception) { "WebSocket error for ${session.id}" }
        sessions.remove(session.id)
    }

    fun broadcastStatus() {
        val status = getCurrentStatus()
        val message = objectMapper.writeValueAsString(status)

        sessions.values.forEach { session ->
            try {
                if (session.isOpen) {
                    session.sendMessage(TextMessage(message))
                }
            } catch (e: Exception) {
                logger.error(e) { "Failed to send status update to ${session.id}" }
                sessions.remove(session.id)
            }
        }
    }

    private fun sendInitialStatus(session: WebSocketSession) {
        val status = getCurrentStatus()
        val message = objectMapper.writeValueAsString(status)

        try {
            session.sendMessage(TextMessage(message))
        } catch (e: Exception) {
            logger.error(e) { "Failed to send initial status" }
        }
    }

    private fun getCurrentStatus(): SystemStatusResponse {
        val wgStatus = runBlocking { wireGuardManager.getStatus() }
        val sbStatus = singBoxManager.status

        return SystemStatusResponse(
            wireGuardStatus = wgStatus,
            singBoxStatus = sbStatus,
            connectedPeers = 0, // Could be implemented by counting active peers
            totalTransferRx = 0,
            totalTransferTx = 0,
            uptime = sbStatus?.let { singBoxManager.uptime } ?: 0,
            cpuUsage = null, // Could be implemented via system monitoring
            memoryUsage = null,
            diskUsage = null,
            timestamp = LocalDateTime.now().toString()
        )
    }

    fun getSessionCount(): Int = sessions.size
}
