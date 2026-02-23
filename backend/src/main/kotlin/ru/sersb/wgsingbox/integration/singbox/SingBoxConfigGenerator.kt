package ru.sersb.wgsingbox.integration.singbox

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Component
import ru.sersb.wgsingbox.model.entity.ProxyConfig
import ru.sersb.wgsingbox.model.entity.RoutingRule

@Component
class SingBoxConfigGenerator(
    @Value("\${singbox.tun.interface-name:tun0}")
    private val tunInterfaceName: String,

    @Value("\${singbox.tun.inet4-address:198.18.0.1/16}")
    private val tunInet4Address: String,

    @Value("\${singbox.mixed.listen-port:2080}")
    private val mixedListenPort: Int,

    @Value("\${singbox.mixed.listen-address:127.0.0.1}")
    private val mixedListenAddress: String
) {
    private val logger = KotlinLogging.logger {}
    private val objectMapper = ObjectMapper()

    init {
        objectMapper.findAndRegisterModules()
    }

    fun generateConfig(
        proxyConfig: ProxyConfig?,
        rules: List<RoutingRule>,
        customTemplate: String? = null
    ): String {
        val configJson = if (!customTemplate.isNullOrEmpty()) {
            try {
                objectMapper.readTree(customTemplate)
            } catch (e: Exception) {
                logger.warn { "Failed to parse custom template, using default: ${e.message}" }
                loadDefaultTemplate()
            }
        } else {
            loadDefaultTemplate()
        }

        // Update proxy outbound
        if (proxyConfig != null) {
            updateProxyOutbound(configJson, proxyConfig)
        } else {
            // Remove or disable proxy outbound
            removeProxyOutbound(configJson)
        }

        // Update routing rules
        updateRoutingRules(configJson, rules, proxyConfig != null)

        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(configJson)
    }

    private fun loadDefaultTemplate(): JsonNode {
        return try {
            val resource = ClassPathResource("singbox-template.json")
            objectMapper.readTree(resource.inputStream)
        } catch (e: Exception) {
            logger.error(e) { "Failed to load default template: ${e.message}" }
            // Return minimal config
            objectMapper.createObjectNode()
        }
    }

    private fun updateProxyOutbound(config: JsonNode, proxy: ProxyConfig) {
        val outbounds = config.path("outbounds") ?: return
        val mutableOutbounds = outbounds as com.fasterxml.jackson.databind.node.ArrayNode

        // Find and update or add proxy outbound
        var proxyOutbound = mutableOutbounds.firstOrNull {
            it.path("tag").asText() == "proxy-out"
        }

        val proxyNode = objectMapper.createObjectNode()
        proxyNode.put("type", proxy.type.name.lowercase())
        proxyNode.put("tag", "proxy-out")

        when (proxy.type) {
            ru.sersb.wgsingbox.model.enum.ProxyType.TROJAN -> {
                proxyNode.put("server", proxy.server)
                proxyNode.put("server_port", proxy.serverPort)
                proxyNode.put("password", proxy.password ?: "")
                proxyNode.put("network", proxy.network.name.lowercase())

                val tls = proxyNode.putObject("tls")
                tls.put("enabled", true)
                tls.put("server_name", proxy.serverName ?: proxy.server)
                tls.put("insecure", proxy.insecure)
            }

            ru.sersb.wgsingbox.model.enum.ProxyType.VLESS -> {
                proxyNode.put("server", proxy.server)
                proxyNode.put("server_port", proxy.serverPort)
                proxyNode.put("uuid", proxy.uuid ?: "")
                proxyNode.put("network", proxy.network.name.lowercase())

                if (proxy.flow != null) {
                    proxyNode.put("flow", proxy.flow)
                }

                val tls = proxyNode.putObject("tls")
                tls.put("enabled", true)
                tls.put("server_name", proxy.serverName ?: proxy.server)
                tls.put("insecure", proxy.insecure)
            }

            ru.sersb.wgsingbox.model.enum.ProxyType.VMESS -> {
                proxyNode.put("server", proxy.server)
                proxyNode.put("server_port", proxy.serverPort)
                proxyNode.put("uuid", proxy.uuid ?: "")
                proxyNode.put("alter_id", proxy.alterId)
                proxyNode.put("security", proxy.security.lowercase())
                proxyNode.put("network", proxy.network.name.lowercase())

                val tls = proxyNode.putObject("tls")
                tls.put("enabled", true)
                tls.put("server_name", proxy.serverName ?: proxy.server)
                tls.put("insecure", proxy.insecure)
            }

            ru.sersb.wgsingbox.model.enum.ProxyType.SHADOWSOCKS -> {
                proxyNode.put("server", proxy.server)
                proxyNode.put("server_port", proxy.serverPort)
                proxyNode.put("method", proxy.method.value)
                proxyNode.put("password", proxy.password ?: "")
            }
        }

        if (proxyOutbound != null) {
            // Replace existing
            val index = mutableOutbounds.indexOf(proxyOutbound)
            mutableOutbounds.set(index, proxyNode)
        } else {
            // Add new
            mutableOutbounds.add(proxyNode)
        }
    }

    private fun removeProxyOutbound(config: JsonNode) {
        val outbounds = config.path("outbounds") ?: return
        val mutableOutbounds = outbounds as com.fasterxml.jackson.databind.node.ArrayNode

        val iterator = mutableOutbounds.iterator()
        while (iterator.hasNext()) {
            val outbound = iterator.next()
            if (outbound.path("tag").asText() == "proxy-out") {
                iterator.remove()
            }
        }
    }

    private fun updateRoutingRules(config: JsonNode, rules: List<RoutingRule>, hasProxy: Boolean) {
        val route = config.path("route") as? com.fasterxml.jackson.databind.node.ObjectNode ?: return
        val ruleArray = route.path("rules") as? com.fasterxml.jackson.databind.node.ArrayNode
            ?: return

        // Clear existing custom rules (keep DNS and private IPs)
        val customRules = mutableListOf<JsonNode>()
        val iterator = ruleArray.iterator()
        while (iterator.hasNext()) {
            val rule = iterator.next()
            val protocol = rule.path("protocol")?.asText()
            val ipIsPrivate = rule.path("ip_is_private")?.asBoolean()
            if (protocol != "dns" && ipIsPrivate != true) {
                customRules.add(rule)
                iterator.remove()
            }
        }

        // Add new rules
        rules.filter { it.enabled }.sortedBy { it.priority }.forEach { dbRule ->
            val ruleNode = objectMapper.createObjectNode()

            when (dbRule.type) {
                ru.sersb.wgsingbox.model.enum.RuleType.DOMAIN -> {
                    try {
                        val domains = objectMapper.readValue<List<String>>(dbRule.value)
                        if (domains.isNotEmpty()) {
                            val domainArray = ruleNode.putArray("domain")
                            domains.forEach { domainArray.add(it) }
                        }
                    } catch (e: Exception) {
                        logger.warn { "Failed to parse domain list: ${e.message}" }
                        ruleNode.put("domain", dbRule.value)
                    }
                }

                ru.sersb.wgsingbox.model.enum.RuleType.IP_CIDR -> {
                    try {
                        val cidrs = objectMapper.readValue<List<String>>(dbRule.value)
                        if (cidrs.isNotEmpty()) {
                            val cidrArray = ruleNode.putArray("ip_cidr")
                            cidrs.forEach { cidrArray.add(it) }
                        }
                    } catch (e: Exception) {
                        logger.warn { "Failed to parse CIDR list: ${e.message}" }
                        ruleNode.put("ip_cidr", dbRule.value)
                    }
                }

                ru.sersb.wgsingbox.model.enum.RuleType.GEOSITE -> {
                    try {
                        val categories = objectMapper.readValue<List<String>>(dbRule.value)
                        if (categories.isNotEmpty()) {
                            val geoArray = ruleNode.putArray("geosite")
                            categories.forEach { geoArray.add(it) }
                        }
                    } catch (e: Exception) {
                        logger.warn { "Failed to parse geosite list: ${e.message}" }
                        ruleNode.put("geosite", dbRule.value)
                    }
                }
            }

            // Map outbound tag
            val outboundTag = when (dbRule.outboundTag) {
                "proxy" -> if (hasProxy) "proxy-out" else "direct"
                "direct" -> "direct"
                "block" -> "block"
                else -> dbRule.outboundTag
            }
            ruleNode.put("outbound", outboundTag)

            ruleArray.add(ruleNode)
        }
    }

    fun validateConfig(config: String): Result<Unit> {
        return try {
            objectMapper.readTree(config)
            Result.success(Unit)
        } catch (e: Exception) {
            logger.error(e) { "Config validation failed: ${e.message}" }
            Result.failure(e)
        }
    }
}
