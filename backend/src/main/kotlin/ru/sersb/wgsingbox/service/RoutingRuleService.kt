package ru.sersb.wgsingbox.service

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service
import ru.sersb.wgsingbox.integration.singbox.SingBoxManager
import ru.sersb.wgsingbox.model.dto.request.RoutingRuleRequest
import ru.sersb.wgsingbox.model.dto.response.RoutingRuleResponse
import ru.sersb.wgsingbox.model.entity.RoutingRule

@Service
class RoutingRuleService(
    private val routingRuleRepository: ru.sersb.wgsingbox.repository.RoutingRuleRepository,
    private val proxyConfigRepository: ru.sersb.wgsingbox.repository.ProxyConfigRepository,
    private val singBoxManager: SingBoxManager
) {
    private val logger = KotlinLogging.logger {}

    fun getAll(): List<RoutingRuleResponse> {
        return routingRuleRepository.findAllByOrderByPriorityAsc()
            .map { RoutingRuleResponse.fromEntity(it) }
    }

    fun getById(id: Long): RoutingRuleResponse {
        val rule = routingRuleRepository.findById(id)
            .orElseThrow { ru.sersb.wgsingbox.model.exception.ResourceNotFoundException("Routing rule not found") }
        return RoutingRuleResponse.fromEntity(rule)
    }

    fun create(request: RoutingRuleRequest): RoutingRuleResponse {
        val rule = RoutingRule(
            type = request.type,
            value = request.value,
            outboundTag = request.outboundTag,
            enabled = request.enabled,
            priority = request.priority,
            description = request.description
        )

        val saved = routingRuleRepository.save(rule)
        logger.info { "Created routing rule: ${saved.type} - ${saved.outboundTag}" }

        restartSingbox()

        return RoutingRuleResponse.fromEntity(saved)
    }

    fun update(id: Long, request: RoutingRuleRequest): RoutingRuleResponse {
        val rule = routingRuleRepository.findById(id)
            .orElseThrow { ru.sersb.wgsingbox.model.exception.ResourceNotFoundException("Routing rule not found") }

        rule.apply {
            type = request.type
            value = request.value
            outboundTag = request.outboundTag
            enabled = request.enabled
            priority = request.priority
            description = request.description
        }

        val saved = routingRuleRepository.save(rule)
        logger.info { "Updated routing rule: ${saved.type} - ${saved.outboundTag}" }

        restartSingbox()

        return RoutingRuleResponse.fromEntity(saved)
    }

    fun delete(id: Long) {
        val rule = routingRuleRepository.findById(id)
            .orElseThrow { ru.sersb.wgsingbox.model.exception.ResourceNotFoundException("Routing rule not found") }

        routingRuleRepository.deleteById(id)
        logger.info { "Deleted routing rule: ${rule.type} - ${rule.outboundTag}" }

        restartSingbox()
    }

    fun toggle(id: Long): RoutingRuleResponse {
        val rule = routingRuleRepository.findById(id)
            .orElseThrow { ru.sersb.wgsingbox.model.exception.ResourceNotFoundException("Routing rule not found") }

        rule.enabled = !rule.enabled
        val saved = routingRuleRepository.save(rule)

        logger.info { "Toggled routing rule ${rule.type} - ${rule.outboundTag} to ${if (saved.enabled) "enabled" else "disabled"}" }

        restartSingbox()

        return RoutingRuleResponse.fromEntity(saved)
    }

    fun reorder(requests: List<RoutingRuleRequest>): List<RoutingRuleResponse> {
        requests.forEachIndexed { index, req ->
            val rule = routingRuleRepository.findById(req.type.hashCode().toLong()) // Simplified
            rule?.let {
                it.priority = (index + 1) * 100
                routingRuleRepository.save(it)
            }
        }

        val all = routingRuleRepository.findAllByOrderByPriorityAsc()
        logger.info { "Reordered ${all.size} routing rules" }

        restartSingbox()

        return all.map { RoutingRuleResponse.fromEntity(it) }
    }

    fun getAvailableGeoSiteCategories(): List<String> {
        return listOf(
            "category-ads-all",
            "category-porn",
            "cn",
            "github",
            "google",
            "openai",
            "netflix",
            "telegram",
            "twitter",
            "youtube",
            "private"
        )
    }

    private fun restartSingbox() {
        val activeProxy = proxyConfigRepository.findByEnabledTrueOrderByPriorityAsc().firstOrNull()
        val rules = routingRuleRepository.findByEnabledTrueOrderByPriorityAsc()

        runBlocking {
            singBoxManager.restart(activeProxy, rules)
        }
    }
}
