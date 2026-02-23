package ru.sersb.wgsingbox.service

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service
import ru.sersb.wgsingbox.integration.singbox.SingBoxManager
import ru.sersb.wgsingbox.model.dto.response.SingboxStatusResponse
import ru.sersb.wgsingbox.model.enum.ServiceStatus

@Service
class SingboxService(
    private val singBoxManager: SingBoxManager,
    private val proxyConfigRepository: ru.sersb.wgsingbox.repository.ProxyConfigRepository,
    private val routingRuleRepository: ru.sersb.wgsingbox.repository.RoutingRuleRepository
) {
    private val logger = KotlinLogging.logger {}

    fun getStatus(): SingboxStatusResponse {
        val status = singBoxManager.getStatus()
        val activeProxy = proxyConfigRepository.findByEnabledTrueOrderByPriorityAsc()
            .firstOrNull()
            ?.let { ru.sersb.wgsingbox.model.dto.response.ProxyConfigResponse.fromEntity(it) }

        return SingboxStatusResponse(
            status = status,
            activeProxy = activeProxy,
            uptime = singBoxManager.getUptime(),
            memoryUsage = null // Could be implemented via process monitoring
        )
    }

    fun start(): SingboxStatusResponse {
        val activeProxy = proxyConfigRepository.findByEnabledTrueOrderByPriorityAsc().firstOrNull()
        val rules = routingRuleRepository.findByEnabledTrueOrderByPriorityAsc()

        runBlocking {
            singBoxManager.start(activeProxy, rules)
        }

        logger.info { "Singbox started" }

        return getStatus()
    }

    fun stop(): SingboxStatusResponse {
        runBlocking {
            singBoxManager.stop()
        }

        logger.info { "Singbox stopped" }

        return getStatus()
    }

    fun restart(): SingboxStatusResponse {
        val activeProxy = proxyConfigRepository.findByEnabledTrueOrderByPriorityAsc().firstOrNull()
        val rules = routingRuleRepository.findByEnabledTrueOrderByPriorityAsc()

        runBlocking {
            singBoxManager.restart(activeProxy, rules)
        }

        logger.info { "Singbox restarted" }

        return getStatus()
    }
}
