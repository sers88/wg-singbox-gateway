package ru.sersb.wgsingbox.integration.wireguard

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import ru.sersb.wgsingbox.model.entity.Peer
import ru.sersb.wgsingbox.model.entity.WireGuardConfig

@Component
class WireGuardConfigGenerator(
    @Value("\${wireguard.default.post-up:}")
    private val defaultPostUp: String,

    @Value("\${wireguard.default.post-down:}")
    private val defaultPostDown: String
) {
    private val logger = KotlinLogging.logger {}

    fun generateConfig(config: WireGuardConfig, peers: List<Peer>): String {
        val postUp = config.postUp ?: defaultPostUp
        val postDown = config.postDown ?: defaultPostDown

        val template = buildString {
            appendLine("[Interface]")
            appendLine("PrivateKey = ${config.privateKey}")
            appendLine("Address = ${config.address}")
            appendLine("ListenPort = ${config.listenPort}")
            appendLine("MTU = ${config.mtu}")

            if (!postUp.isNullOrBlank()) {
                appendLine("PostUp = $postUp")
            }

            if (!postDown.isNullOrBlank()) {
                appendLine("PostDown = $postDown")
            }

            appendLine()

            peers.forEach { peer ->
                appendLine("[Peer]")
                appendLine("PublicKey = ${peer.publicKey}")

                if (!peer.presharedKey.isNullOrBlank()) {
                    appendLine("PresharedKey = ${peer.presharedKey}")
                }

                appendLine("AllowedIPs = ${peer.allowedIps}")

                if (peer.persistentKeepalive > 0) {
                    appendLine("PersistentKeepalive = ${peer.persistentKeepalive}")
                }

                appendLine()
            }
        }

        logger.debug { "Generated WireGuard config for interface ${config.interfaceName}" }
        return template
    }

    fun generatePeerClientConfig(
        serverConfig: WireGuardConfig,
        peer: Peer,
        serverEndpoint: String? = null
    ): String {
        val serverIp = serverEndpoint ?: "<server-ip>"

        val template = buildString {
            appendLine("[Interface]")
            appendLine("PrivateKey = <client-private-key>")
            appendLine("Address = ${peer.allowedIps.split(",").firstOrNull() ?: "10.0.0.2/32"}")
            appendLine("DNS = ${serverConfig.address.split("/").firstOrNull() ?: "10.0.0.1"}")
            appendLine()

            appendLine("[Peer]")
            appendLine("PublicKey = ${serverConfig.publicKey}")
            appendLine("Endpoint = $serverIp:${serverConfig.listenPort}")

            if (!peer.presharedKey.isNullOrBlank()) {
                appendLine("PresharedKey = ${peer.presharedKey}")
            }

            appendLine("AllowedIPs = 0.0.0.0/0")

            if (peer.persistentKeepalive > 0) {
                appendLine("PersistentKeepalive = ${peer.persistentKeepalive}")
            }
        }

        logger.debug { "Generated client config for peer ${peer.name}" }
        return template
    }

    fun generateWireGuardURI(
        serverConfig: WireGuardConfig,
        peer: Peer,
        serverEndpoint: String? = null
    ): String {
        val serverIp = serverEndpoint ?: "<server-ip>"
        val dns = serverConfig.address.split("/").firstOrNull() ?: "10.0.0.1"
        val clientAddress = peer.allowedIps.split(",").firstOrNull() ?: "10.0.0.2/32"

        // WireGuard URI format: wg://private_key@public_key:port/?allowed_ips=...&dns=...
        val uri = buildString {
            append("wg://")
            append("<client-private-key>@")
            append("$serverIp:")
            append(serverConfig.listenPort)
            append("?allowed_ips=0.0.0.0/0")
            append("&dns=$dns")
            if (!peer.presharedKey.isNullOrBlank()) {
                append("&preshared_key=${peer.presharedKey}")
            }
            if (peer.persistentKeepalive > 0) {
                append("&persistent_keepalive=${peer.persistentKeepalive}")
            }
        }

        return uri
    }
}
