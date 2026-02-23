package ru.sersb.wgsingbox.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry

@Configuration
@EnableWebSocket
class WebSocketConfig(
    private val statusWebSocketHandler: ru.sersb.wgsingbox.websocket.StatusWebSocketHandler,
    private val logWebSocketHandler: ru.sersb.wgsingbox.websocket.LogWebSocketHandler
) : WebSocketConfigurer {

    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry.addHandler(statusWebSocketHandler, "/ws/status")
            .setAllowedOriginPatterns("*")

        registry.addHandler(logWebSocketHandler, "/ws/logs")
            .setAllowedOriginPatterns("*")
    }
}
