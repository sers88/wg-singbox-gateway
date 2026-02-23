package ru.sersb.wgsingbox.service

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service
import ru.sersb.wgsingbox.integration.singbox.SingBoxManager
import ru.sersb.wgsingbox.model.dto.request.ProxyConfigRequest
import ru.sersb.wgsingbox.model.dto.response.ProxyConfigResponse
import ru.sersb.wgsingbox.model.exception.ValidationException
import ru.sersb.wgsingbox.model.entity.ProxyConfig

@Service
class ProxyConfigService(
    private val proxyConfigRepository: ru.sersb.wgsingbox.repository.ProxyConfigRepository,
    private val routingRuleRepository: ru.sersb.wgsingbox.repository.RoutingRuleRepository,
    private val singBoxManager: SingBoxManager
) {
    private val logger = KotlinLogging.logger {}

    fun getAll(): List<ProxyConfigResponse> {
        return proxyConfigRepository.findAllByOrderByPriorityAsc()
            .map { ProxyConfigResponse.fromEntity(it) }
    }

    fun getById(id: Long): ProxyConfigResponse {
        val proxy = proxyConfigRepository.findById(id)
            .orElseThrow { ru.sersb.wgsingbox.model.exception.ResourceNotFoundException("Proxy configuration not found") }
        return ProxyConfigResponse.fromEntity(proxy)
    }

    fun create(request: ProxyConfigRequest): ProxyConfigResponse {
        val proxy = ProxyConfig(
            type = request.type,
            server = request.server,
            serverPort = request.serverPort,
            password = request.password,
            uuid = request.uuid,
            serverName = request.serverName,
            insecure = request.insecure,
            network = request.network,
            flow = request.flow,
            alterId = request.alterId,
            security = request.security,
            method = request.method,
            enabled = request.enabled,
            priority = request.priority
        )

        val saved = proxyConfigRepository.save(proxy)
        logger.info { "Created proxy configuration: ${saved.type} - ${saved.server}" }

        return ProxyConfigResponse.fromEntity(saved)
    }

    fun update(id: Long, request: ProxyConfigRequest): ProxyConfigResponse {
        val proxy = proxyConfigRepository.findById(id)
            .orElseThrow { ru.sersb.wgsingbox.model.exception.ResourceNotFoundException("Proxy configuration not found") }

        proxy.apply {
            type = request.type
            server = request.server
            serverPort = request.serverPort
            password = request.password
            uuid = request.uuid
            serverName = request.serverName
            insecure = request.insecure
            network = request.network
            flow = request.flow
            alterId = request.alterId
            security = request.security
            method = request.method
            enabled = request.enabled
            priority = request.priority
        }

        val saved = proxyConfigRepository.save(proxy)
        logger.info { "Updated proxy configuration: ${saved.type} - ${saved.server}" }

        // If this is the active proxy and Singbox is running, restart
        if (saved.enabled) {
            restartSingbox()
        }

        return ProxyConfigResponse.fromEntity(saved)
    }

    fun delete(id: Long) {
        val proxy = proxyConfigRepository.findById(id)
            .orElseThrow { ru.sersb.wgsingbox.model.exception.ResourceNotFoundException("Proxy configuration not found") }

        proxyConfigRepository.deleteById(id)
        logger.info { "Deleted proxy configuration: ${proxy.type} - ${proxy.server}" }

        restartSingbox()
    }

    fun toggle(id: Long): ProxyConfigResponse {
        val proxy = proxyConfigRepository.findById(id)
            .orElseThrow { ru.sersb.wgsingbox.model.exception.ResourceNotFoundException("Proxy configuration not found") }

        proxy.enabled = !proxy.enabled
        val saved = proxyConfigRepository.save(proxy)

        logger.info { "Toggled proxy configuration ${proxy.type} - ${proxy.server} to ${if (saved.enabled) "enabled" else "disabled"}" }

        restartSingbox()

        return ProxyConfigResponse.fromEntity(saved)
    }

    fun setActive(id: Long): ProxyConfigResponse {
        val proxy = proxyConfigRepository.findById(id)
            .orElseThrow { ru.sersb.wgsingbox.model.exception.ResourceNotFoundException("Proxy configuration not found") }

        // Disable all other proxies
        proxyConfigRepository.findAll().forEach { it.enabled = false }
        proxyConfigRepository.saveAll(proxyConfigRepository.findAll())

        // Enable this one
        proxy.enabled = true
        val saved = proxyConfigRepository.save(proxy)

        logger.info { "Set active proxy: ${saved.type} - ${saved.server}" }

        restartSingbox()

        return ProxyConfigResponse.fromEntity(saved)
    }

    fun testConnection(id: Long): Map<String, Any> {
        val proxy = proxyConfigRepository.findById(id)
            .orElseThrow { ru.sersb.wgsingbox.model.exception.ResourceNotFoundException("Proxy configuration not found") }

        // Basic connectivity test
        val result = mapOf(
            "success" to true,
            "message" to "Configuration validated (actual connection test not implemented)",
            "server" to proxy.server,
            "port" to proxy.serverPort,
            "type" to proxy.type.name
        )

        logger.info { "Tested proxy connection: ${proxy.type} - ${proxy.server}" }

        return result
    }

    private fun restartSingbox() {
        val activeProxy = proxyConfigRepository.findByEnabledTrueOrderByPriorityAsc().firstOrNull()
        val rules = routingRuleRepository.findByEnabledTrueOrderByPriorityAsc()

        runBlocking {
            singBoxManager.restart(activeProxy, rules)
        }
    }
}
