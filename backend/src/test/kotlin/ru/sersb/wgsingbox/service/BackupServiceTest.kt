package ru.sersb.wgsingbox.service

import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import ru.sersb.wgsingbox.model.dto.response.ConfigBackupResponse
import ru.sersb.wgsingbox.model.entity.ConfigBackup
import ru.sersb.wgsingbox.model.entity.Peer
import ru.sersb.wgsingbox.model.entity.ProxyConfig
import ru.sersb.wgsingbox.model.entity.RoutingRule
import ru.sersb.wgsingbox.model.entity.WireGuardConfig
import ru.sersb.wgsingbox.model.enum.ProxyType
import ru.sersb.wgsingbox.model.enum.RuleType
import ru.sersb.wgsingbox.model.exception.ResourceNotFoundException
import java.time.LocalDateTime
import java.util.Optional

class BackupServiceTest {

    private lateinit var backupService: BackupService
    private lateinit var configBackupRepository: ru.sersb.wgsingbox.repository.ConfigBackupRepository
    private lateinit var wireGuardConfigRepository: ru.sersb.wgsingbox.repository.WireGuardConfigRepository
    private lateinit var peerRepository: ru.sersb.wgsingbox.repository.PeerRepository
    private lateinit var proxyConfigRepository: ru.sersb.wgsingbox.repository.ProxyConfigRepository
    private lateinit var routingRuleRepository: ru.sersb.wgsingbox.repository.RoutingRuleRepository

    @BeforeEach
    fun setUp() {
        configBackupRepository = mockk()
        wireGuardConfigRepository = mockk()
        peerRepository = mockk()
        proxyConfigRepository = mockk()
        routingRuleRepository = mockk()

        backupService = BackupService(
            configBackupRepository,
            wireGuardConfigRepository,
            peerRepository,
            proxyConfigRepository,
            routingRuleRepository
        )
    }

    @Test
    fun `createBackup should save backup successfully`() {
        // Arrange
        val wgConfig = WireGuardConfig(
            privateKey = "private-key",
            publicKey = "public-key",
            listenPort = 51820,
            address = "10.0.0.1/24",
            enabled = true,
            interfaceName = "wg0"
        )
        wgConfig.id = 1L

        val peers = listOf(
            Peer(
                publicKey = "peer-key",
                allowedIps = "10.0.0.2/32",
                name = "Test Peer",
                enabled = true
            )
        )

        val proxyConfigs = listOf(
            ProxyConfig(
                type = ProxyType.TROJAN,
                server = "example.com",
                serverPort = 443,
                password = "password",
                enabled = true
            )
        )

        val routingRules = listOf(
            RoutingRule(
                type = RuleType.DOMAIN,
                value = "example.com",
                outboundTag = "direct",
                enabled = true
            )
        )

        val savedBackup = ConfigBackup(
            name = "Test Backup",
            description = "Test description",
            data = "{\"test\":\"data\"}"
        )
        savedBackup.id = 1L

        every { wireGuardConfigRepository.findFirstByOrderByCreatedAtAsc() } returns wgConfig
        every { peerRepository.findByConfigIdOrderByCreatedAtAsc(1L) } returns peers
        every { proxyConfigRepository.findAll() } returns proxyConfigs
        every { routingRuleRepository.findAll() } returns routingRules
        every { configBackupRepository.save(any()) } returns savedBackup

        // Act
        val result = backupService.createBackup("Test Backup", "Test description")

        // Assert
        assertEquals("Test Backup", result.name)
        assertEquals("Test description", result.description)
        assertEquals(1L, result.id)

        verify { wireGuardConfigRepository.findFirstByOrderByCreatedAtAsc() }
        verify { peerRepository.findByConfigIdOrderByCreatedAtAsc(1L) }
        verify { proxyConfigRepository.findAll() }
        verify { routingRuleRepository.findAll() }
        verify { configBackupRepository.save(any()) }
    }

    @Test
    fun `getBackups should return all backups`() {
        // Arrange
        val backups = listOf(
            ConfigBackup(name = "Backup 1", description = null, data = "{}"),
            ConfigBackup(name = "Backup 2", description = "Desc", data = "{}")
        )
        backups[0].id = 1L
        backups[1].id = 2L

        every { configBackupRepository.findAllByOrderByCreatedAtDesc() } returns backups

        // Act
        val result = backupService.getBackups()

        // Assert
        assertEquals(2, result.size)
        assertEquals("Backup 1", result[0].name)
        assertEquals("Backup 2", result[1].name)

        verify { configBackupRepository.findAllByOrderByCreatedAtDesc() }
    }

    @Test
    fun `deleteBackup should call repository delete`() {
        // Arrange
        every { configBackupRepository.deleteById(1L) } just Runs

        // Act
        backupService.deleteBackup(1L)

        // Assert
        verify { configBackupRepository.deleteById(1L) }
    }

    @Test
    fun `restoreBackup should throw exception when backup not found`() {
        // Arrange
        every { configBackupRepository.findById(999L) } returns Optional.empty()

        // Act & Assert
        assertThrows<ResourceNotFoundException> {
            backupService.restoreBackup(999L)
        }

        verify { configBackupRepository.findById(999L) }
    }

    @Test
    fun `createBackup should handle null WireGuard config`() {
        // Arrange
        val savedBackup = ConfigBackup(
            name = "Test Backup",
            description = null,
            data = "{\"test\":\"data\"}"
        )
        savedBackup.id = 1L

        every { wireGuardConfigRepository.findFirstByOrderByCreatedAtAsc() } returns null
        every { proxyConfigRepository.findAll() } returns emptyList()
        every { routingRuleRepository.findAll() } returns emptyList()
        every { configBackupRepository.save(any()) } returns savedBackup

        // Act
        val result = backupService.createBackup("Test Backup")

        // Assert
        assertEquals("Test Backup", result.name)
        assertEquals(1L, result.id)

        verify { wireGuardConfigRepository.findFirstByOrderByCreatedAtAsc() }
        verify(exactly = 0) { peerRepository.findByConfigIdOrderByCreatedAtAsc(any()) }
        verify { configBackupRepository.save(any()) }
    }
}
