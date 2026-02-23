package ru.sersb.wgsingbox.service

import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import ru.sersb.wgsingbox.model.dto.request.PeerRequest
import ru.sersb.wgsingbox.model.dto.request.WireGuardConfigRequest
import ru.sersb.wgsingbox.model.dto.response.PeerResponse
import ru.sersb.wgsingbox.model.dto.response.WireGuardConfigResponse
import ru.sersb.wgsingbox.model.dto.response.WireGuardStatusResponse
import ru.sersb.wgsingbox.model.entity.Peer
import ru.sersb.wgsingbox.model.entity.WireGuardConfig
import ru.sersb.wgsingbox.model.enum.ServiceStatus
import ru.sersb.wgsingbox.model.exception.ResourceNotFoundException
import ru.sersb.wgsingbox.model.exception.ValidationException
import ru.sersb.wgsingbox.integration.wireguard.WireGuardManager
import ru.sersb.wgsingbox.integration.wireguard.WireGuardConfigGenerator
import java.util.Optional

class WireGuardServiceTest {

    private lateinit var wireGuardService: WireGuardService
    private lateinit var wireGuardConfigRepository: ru.sersb.wgsingbox.repository.WireGuardConfigRepository
    private lateinit var peerRepository: ru.sersb.wgsingbox.repository.PeerRepository
    private lateinit var wireGuardManager: WireGuardManager
    private lateinit var configGenerator: WireGuardConfigGenerator

    @BeforeEach
    fun setUp() {
        wireGuardConfigRepository = mockk()
        peerRepository = mockk()
        wireGuardManager = mockk()
        configGenerator = mockk()

        wireGuardService = WireGuardService(
            wireGuardConfigRepository,
            peerRepository,
            wireGuardManager,
            configGenerator
        )
    }

    @Test
    fun `getConfig should return config when exists`() {
        // Arrange
        val config = WireGuardConfig(
            privateKey = "private-key",
            publicKey = "public-key",
            listenPort = 51820,
            address = "10.0.0.1/24",
            mtu = 1280,
            postUp = null,
            postDown = null,
            enabled = true,
            interfaceName = "wg0"
        )
        config.id = 1L

        coEvery { wireGuardManager.getStatus() } returns ServiceStatus.RUNNING
        every { wireGuardConfigRepository.findByEnabledTrue() } returns config

        // Act
        val result = wireGuardService.getConfig()

        // Assert
        assertEquals("private-key", result.privateKey)
        assertEquals("public-key", result.publicKey)
        assertEquals(51820, result.listenPort)
        assertEquals(ServiceStatus.RUNNING, result.status)

        verify { wireGuardConfigRepository.findByEnabledTrue() }
        coVerify { wireGuardManager.getStatus() }
    }

    @Test
    fun `getConfig should throw exception when not found`() {
        // Arrange
        every { wireGuardConfigRepository.findByEnabledTrue() } returns null
        every { wireGuardConfigRepository.findFirstByOrderByCreatedAtAsc() } returns null

        // Act & Assert
        assertThrows<ResourceNotFoundException> {
            wireGuardService.getConfig()
        }

        verify { wireGuardConfigRepository.findByEnabledTrue() }
        verify { wireGuardConfigRepository.findFirstByOrderByCreatedAtAsc() }
    }

    @Test
    fun `getStatus should return status with peers`() {
        // Arrange
        val config = WireGuardConfig(
            privateKey = "private-key",
            publicKey = "public-key",
            listenPort = 51820,
            address = "10.0.0.1/24",
            enabled = true,
            interfaceName = "wg0"
        )
        config.id = 1L

        val wgPeer = ru.sersb.wgsingbox.integration.wireguard.Peer(
            publicKey = "peer-key",
            endpoint = "192.168.1.100:54321",
            allowedIps = "10.0.0.2/32",
            latestHandshake = null,
            transferRx = 1024,
            transferTx = 2048
        )

        val wgStatus = ru.sersb.wgsingbox.integration.wireguard.WireGuardStatus(
            publicKey = "public-key",
            listenPort = 51820,
            peers = listOf(wgPeer)
        )

        coEvery { wireGuardManager.getDetailedStatus() } returns Pair(ServiceStatus.RUNNING, wgStatus)
        every { wireGuardConfigRepository.findByEnabledTrue() } returns config

        // Act
        val result = wireGuardService.getStatus()

        // Assert
        assertEquals(ServiceStatus.RUNNING, result.status)
        assertEquals("wg0", result.interfaceName)
        assertEquals(1, result.peers.size)
        assertEquals("peer-key", result.peers[0].publicKey)
        assertEquals(1024, result.peers[0].transferRx)
        assertEquals(2048, result.peers[0].transferTx)

        coVerify { wireGuardManager.getDetailedStatus() }
    }

    @Test
    fun `startInterface should start interface and return status`() {
        // Arrange
        val config = WireGuardConfig(
            privateKey = "private-key",
            publicKey = "public-key",
            listenPort = 51820,
            address = "10.0.0.1/24",
            enabled = true,
            interfaceName = "wg0"
        )
        config.id = 1L

        coEvery { wireGuardManager.startInterface() } returns Result.success(Unit)
        coEvery { wireGuardManager.getDetailedStatus() } returns Pair(ServiceStatus.RUNNING, null)
        every { wireGuardConfigRepository.findByEnabledTrue() } returns config

        // Act
        val result = wireGuardService.startInterface()

        // Assert
        assertEquals(ServiceStatus.RUNNING, result.status)

        coVerify { wireGuardManager.startInterface() }
        coVerify { wireGuardManager.getDetailedStatus() }
    }

    @Test
    fun `stopInterface should stop interface and return status`() {
        // Arrange
        val config = WireGuardConfig(
            privateKey = "private-key",
            publicKey = "public-key",
            listenPort = 51820,
            address = "10.0.0.1/24",
            enabled = true,
            interfaceName = "wg0"
        )
        config.id = 1L

        coEvery { wireGuardManager.stopInterface() } returns Result.success(Unit)
        coEvery { wireGuardManager.getDetailedStatus() } returns Pair(ServiceStatus.STOPPED, null)
        every { wireGuardConfigRepository.findByEnabledTrue() } returns config

        // Act
        val result = wireGuardService.stopInterface()

        // Assert
        assertEquals(ServiceStatus.STOPPED, result.status)

        coVerify { wireGuardManager.stopInterface() }
        coVerify { wireGuardManager.getDetailedStatus() }
    }

    @Test
    fun `createPeer should save new peer successfully`() {
        // Arrange
        val config = WireGuardConfig(
            privateKey = "private-key",
            publicKey = "public-key",
            listenPort = 51820,
            address = "10.0.0.1/24",
            enabled = true,
            interfaceName = "wg0"
        )
        config.id = 1L

        val peer = Peer(
            config = config,
            publicKey = "peer-public-key",
            presharedKey = null,
            allowedIps = "10.0.0.2/32",
            name = "Test Peer",
            enabled = true
        )
        peer.id = 1L

        coEvery { wireGuardManager.restartInterface() } returns Result.success(Unit)
        every { wireGuardConfigRepository.findFirstByOrderByCreatedAtAsc() } returns config
        every { peerRepository.findByPublicKey(any()) } returns null
        every { peerRepository.save(any()) } returns peer

        val request = PeerRequest(
            publicKey = "peer-public-key",
            presharedKey = null,
            allowedIps = "10.0.0.2/32",
            name = "Test Peer",
            enabled = true
        )

        // Act
        val result = wireGuardService.createPeer(request)

        // Assert
        assertEquals("Test Peer", result.name)
        assertEquals("peer-public-key", result.publicKey)
        assertEquals(1L, result.id)

        verify { wireGuardConfigRepository.findFirstByOrderByCreatedAtAsc() }
        verify { peerRepository.findByPublicKey("peer-public-key") }
        verify { peerRepository.save(any()) }
        coVerify { wireGuardManager.restartInterface() }
    }

    @Test
    fun `createPeer should throw exception when public key already exists`() {
        // Arrange
        val config = WireGuardConfig(
            privateKey = "private-key",
            publicKey = "public-key",
            listenPort = 51820,
            address = "10.0.0.1/24",
            enabled = true,
            interfaceName = "wg0"
        )
        config.id = 1L

        every { wireGuardConfigRepository.findFirstByOrderByCreatedAtAsc() } returns config
        every { peerRepository.findByPublicKey("existing-key") } returns Peer(
            publicKey = "existing-key",
            allowedIps = "10.0.0.2/32",
            name = "Existing Peer",
            enabled = true
        )

        val request = PeerRequest(
            publicKey = "existing-key",
            presharedKey = null,
            allowedIps = "10.0.0.2/32",
            name = "Test Peer",
            enabled = true
        )

        // Act & Assert
        assertThrows<ValidationException> {
            wireGuardService.createPeer(request)
        }

        verify { wireGuardConfigRepository.findFirstByOrderByCreatedAtAsc() }
        verify { peerRepository.findByPublicKey("existing-key") }
        verify(exactly = 0) { peerRepository.save(any()) }
    }

    @Test
    fun `deletePeer should remove peer`() {
        // Arrange
        val config = WireGuardConfig(
            privateKey = "private-key",
            publicKey = "public-key",
            listenPort = 51820,
            address = "10.0.0.1/24",
            enabled = true,
            interfaceName = "wg0"
        )
        config.id = 1L

        val peer = Peer(
            config = config,
            publicKey = "peer-key",
            allowedIps = "10.0.0.2/32",
            name = "Test Peer",
            enabled = true
        )
        peer.id = 1L

        coEvery { wireGuardManager.restartInterface() } returns Result.success(Unit)
        every { peerRepository.findById(1L) } returns Optional.of(peer)
        every { peerRepository.deleteById(1L) } just Runs

        // Act
        wireGuardService.deletePeer(1L)

        // Assert
        verify { peerRepository.findById(1L) }
        verify { peerRepository.deleteById(1L) }
        coVerify { wireGuardManager.restartInterface() }
    }

    @Test
    fun `togglePeer should toggle enabled status`() {
        // Arrange
        val config = WireGuardConfig(
            privateKey = "private-key",
            publicKey = "public-key",
            listenPort = 51820,
            address = "10.0.0.1/24",
            enabled = true,
            interfaceName = "wg0"
        )
        config.id = 1L

        val peer = Peer(
            config = config,
            publicKey = "peer-key",
            allowedIps = "10.0.0.2/32",
            name = "Test Peer",
            enabled = true
        )
        peer.id = 1L

        coEvery { wireGuardManager.restartInterface() } returns Result.success(Unit)
        every { peerRepository.findById(1L) } returns Optional.of(peer)
        every { peerRepository.save(any()) } returns peer

        // Act
        val result = wireGuardService.togglePeer(1L)

        // Assert
        assertFalse(result.enabled) // Should be toggled to false
        assertEquals("Test Peer", result.name)

        verify { peerRepository.findById(1L) }
        verify { peerRepository.save(peer) }
        coVerify { wireGuardManager.restartInterface() }
    }

    @Test
    fun `getPeerQRCode should generate client config`() {
        // Arrange
        val config = WireGuardConfig(
            privateKey = "private-key",
            publicKey = "public-key",
            listenPort = 51820,
            address = "10.0.0.1/24",
            enabled = true,
            interfaceName = "wg0"
        )
        config.id = 1L

        val peer = Peer(
            publicKey = "peer-key",
            presharedKey = "psk",
            allowedIps = "10.0.0.2/32",
            name = "Test Peer",
            enabled = true
        )
        peer.id = 1L

        val clientConfig = """
            [Interface]
            PrivateKey = <client-private-key>
            Address = 10.0.0.2/32
            DNS = 10.0.0.1

            [Peer]
            PublicKey = public-key
            Endpoint = example.com:51820
            PresharedKey = psk
            AllowedIPs = 0.0.0.0/0
        """.trimIndent()

        every { wireGuardConfigRepository.findByEnabledTrue() } returns config
        every { peerRepository.findById(1L) } returns Optional.of(peer)
        every { configGenerator.generatePeerClientConfig(config, peer, "example.com") } returns clientConfig

        // Act
        val result = wireGuardService.getPeerQRCode(1L, "example.com")

        // Assert
        assertTrue(result.contains("[Interface]"))
        assertTrue(result.contains("PublicKey = public-key"))
        assertTrue(result.contains("Endpoint = example.com:51820"))

        verify { wireGuardConfigRepository.findByEnabledTrue() }
        verify { peerRepository.findById(1L) }
        verify { configGenerator.generatePeerClientConfig(config, peer, "example.com") }
    }
}
