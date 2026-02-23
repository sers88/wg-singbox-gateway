package ru.sersb.wgsingbox.integration.wireguard

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import ru.sersb.wgsingbox.model.entity.Peer
import ru.sersb.wgsingbox.model.entity.WireGuardConfig

class WireGuardConfigGeneratorTest {

    private lateinit var configGenerator: WireGuardConfigGenerator

    @BeforeEach
    fun setUp() {
        configGenerator = WireGuardConfigGenerator(
            defaultPostUp = "iptables -A FORWARD -i %i -j ACCEPT; iptables -A FORWARD -o %i -j ACCEPT",
            defaultPostDown = "iptables -D FORWARD -i %i -j ACCEPT; iptables -D FORWARD -o %i -j ACCEPT"
        )
    }

    @Test
    fun `generateConfig should create valid WireGuard config`() {
        val config = WireGuardConfig(
            privateKey = "test-private-key",
            publicKey = "test-public-key",
            listenPort = 51820,
            address = "10.0.0.1/24",
            mtu = 1280,
            postUp = null,
            postDown = null,
            enabled = true,
            interfaceName = "wg0"
        )

        val result = configGenerator.generateConfig(config, emptyList())

        assertTrue(result.contains("[Interface]"))
        assertTrue(result.contains("PrivateKey = test-private-key"))
        assertTrue(result.contains("Address = 10.0.0.1/24"))
        assertTrue(result.contains("ListenPort = 51820"))
        assertTrue(result.contains("MTU = 1280"))
    }

    @Test
    fun `generateConfig should include default PostUp when config postUp is null`() {
        val config = WireGuardConfig(
            privateKey = "test-private-key",
            publicKey = "test-public-key",
            listenPort = 51820,
            address = "10.0.0.1/24",
            mtu = 1280,
            postUp = null,
            postDown = null,
            enabled = true,
            interfaceName = "wg0"
        )

        val result = configGenerator.generateConfig(config, emptyList())

        assertTrue(result.contains("PostUp = iptables -A FORWARD -i %i -j ACCEPT"))
    }

    @Test
    fun `generateConfig should use config PostUp when set`() {
        val config = WireGuardConfig(
            privateKey = "test-private-key",
            publicKey = "test-public-key",
            listenPort = 51820,
            address = "10.0.0.1/24",
            mtu = 1280,
            postUp = "custom-post-up-command",
            postDown = null,
            enabled = true,
            interfaceName = "wg0"
        )

        val result = configGenerator.generateConfig(config, emptyList())

        assertTrue(result.contains("PostUp = custom-post-up-command"))
        assertFalse(result.contains("iptables -A FORWARD -i %i -j ACCEPT"))
    }

    @Test
    fun `generateConfig should include peers`() {
        val config = WireGuardConfig(
            privateKey = "test-private-key",
            publicKey = "test-public-key",
            listenPort = 51820,
            address = "10.0.0.1/24",
            mtu = 1280,
            postUp = null,
            postDown = null,
            enabled = true,
            interfaceName = "wg0"
        )

        val peer = Peer(
            publicKey = "peer-public-key",
            presharedKey = "peer-preshared-key",
            allowedIps = "10.0.0.2/32",
            name = "Test Peer",
            enabled = true
        )

        val result = configGenerator.generateConfig(config, listOf(peer))

        assertTrue(result.contains("[Peer]"))
        assertTrue(result.contains("PublicKey = peer-public-key"))
        assertTrue(result.contains("PresharedKey = peer-preshared-key"))
        assertTrue(result.contains("AllowedIPs = 10.0.0.2/32"))
    }

    @Test
    fun `generateConfig should include PersistentKeepalive when set`() {
        val config = WireGuardConfig(
            privateKey = "test-private-key",
            publicKey = "test-public-key",
            listenPort = 51820,
            address = "10.0.0.1/24",
            mtu = 1280,
            postUp = null,
            postDown = null,
            enabled = true,
            interfaceName = "wg0"
        )

        val peer = Peer(
            publicKey = "peer-public-key",
            presharedKey = null,
            allowedIps = "10.0.0.2/32",
            name = "Test Peer",
            persistentKeepalive = 25,
            enabled = true
        )

        val result = configGenerator.generateConfig(config, listOf(peer))

        assertTrue(result.contains("PersistentKeepalive = 25"))
    }

    @Test
    fun `generatePeerClientConfig should create valid client config`() {
        val serverConfig = WireGuardConfig(
            privateKey = "server-private-key",
            publicKey = "server-public-key",
            listenPort = 51820,
            address = "10.0.0.1/24",
            mtu = 1280,
            postUp = null,
            postDown = null,
            enabled = true,
            interfaceName = "wg0"
        )

        val peer = Peer(
            publicKey = "peer-public-key",
            presharedKey = "peer-preshared-key",
            allowedIps = "10.0.0.2/32",
            name = "Test Peer",
            enabled = true
        )

        val result = configGenerator.generatePeerClientConfig(serverConfig, peer, "example.com")

        assertTrue(result.contains("[Interface]"))
        assertTrue(result.contains("[Peer]"))
        assertTrue(result.contains("PublicKey = server-public-key"))
        assertTrue(result.contains("Endpoint = example.com:51820"))
        assertTrue(result.contains("AllowedIPs = 0.0.0.0/0"))
        assertTrue(result.contains("PresharedKey = peer-preshared-key"))
    }

    @Test
    fun `generatePeerClientConfig should use default endpoint when not provided`() {
        val serverConfig = WireGuardConfig(
            privateKey = "server-private-key",
            publicKey = "server-public-key",
            listenPort = 51820,
            address = "10.0.0.1/24",
            mtu = 1280,
            postUp = null,
            postDown = null,
            enabled = true,
            interfaceName = "wg0"
        )

        val peer = Peer(
            publicKey = "peer-public-key",
            presharedKey = null,
            allowedIps = "10.0.0.2/32",
            name = "Test Peer",
            enabled = true
        )

        val result = configGenerator.generatePeerClientConfig(serverConfig, peer, null)

        assertTrue(result.contains("Endpoint = <server-ip>:51820"))
    }

    @Test
    fun `generateWireGuardURI should create valid URI`() {
        val serverConfig = WireGuardConfig(
            privateKey = "server-private-key",
            publicKey = "server-public-key",
            listenPort = 51820,
            address = "10.0.0.1/24",
            mtu = 1280,
            postUp = null,
            postDown = null,
            enabled = true,
            interfaceName = "wg0"
        )

        val peer = Peer(
            publicKey = "peer-public-key",
            presharedKey = "peer-preshared-key",
            allowedIps = "10.0.0.2/32",
            name = "Test Peer",
            enabled = true
        )

        val result = configGenerator.generateWireGuardURI(serverConfig, peer, "example.com")

        assertTrue(result.startsWith("wg://"))
        assertTrue(result.contains("example.com:51820"))
        assertTrue(result.contains("allowed_ips=0.0.0.0/0"))
        assertTrue(result.contains("preshared_key=peer-preshared-key"))
    }
}
