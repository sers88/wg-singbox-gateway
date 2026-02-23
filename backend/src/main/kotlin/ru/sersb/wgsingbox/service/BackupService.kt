package ru.sersb.wgsingbox.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import ru.sersb.wgsingbox.model.dto.response.BackupData
import ru.sersb.wgsingbox.model.dto.response.ConfigBackupResponse
import ru.sersb.wgsingbox.model.entity.ConfigBackup
import java.time.LocalDateTime

@Service
class BackupService(
    private val configBackupRepository: ru.sersb.wgsingbox.repository.ConfigBackupRepository,
    private val wireGuardConfigRepository: ru.sersb.wgsingbox.repository.WireGuardConfigRepository,
    private val peerRepository: ru.sersb.wgsingbox.repository.PeerRepository,
    private val proxyConfigRepository: ru.sersb.wgsingbox.repository.ProxyConfigRepository,
    private val routingRuleRepository: ru.sersb.wgsingbox.repository.RoutingRuleRepository
) {
    private val logger = KotlinLogging.logger {}
    private val mapper = jacksonObjectMapper()

    fun createBackup(name: String, description: String? = null): ConfigBackupResponse {
        val wireGuardConfig = wireGuardConfigRepository.findFirstByOrderByCreatedAtAsc()

        val peers = if (wireGuardConfig != null) {
            peerRepository.findByConfigIdOrderByCreatedAtAsc(wireGuardConfig.id!!)
        } else {
            emptyList()
        }

        val backupData = BackupData(
            wireGuardConfig = wireGuardConfig,
            peers = peers,
            proxyConfigs = proxyConfigRepository.findAll(),
            routingRules = routingRuleRepository.findAll()
        )

        val dataJson = mapper.writeValueAsString(backupData)

        val backup = ConfigBackup(
            name = name,
            description = description,
            data = dataJson
        )

        val saved = configBackupRepository.save(backup)
        logger.info { "Created backup: $name" }

        return ConfigBackupResponse.fromEntity(saved)
    }

    fun getBackups(): List<ConfigBackupResponse> {
        return configBackupRepository.findAllByOrderByCreatedAtDesc()
            .map { ConfigBackupResponse.fromEntity(it) }
    }

    fun restoreBackup(id: Long) {
        val backup = configBackupRepository.findById(id)
            .orElseThrow { ru.sersb.wgsingbox.model.exception.ResourceNotFoundException("Backup not found") }

        try {
            val backupData = mapper.readValue(backup.data, BackupData::class.java)

            // Restore WireGuard config
            backupData.wireGuardConfig?.let { config ->
                val existing = wireGuardConfigRepository.findFirstByOrderByCreatedAtAsc()
                if (existing != null) {
                    existing.apply {
                        privateKey = config.privateKey
                        publicKey = config.publicKey
                        listenPort = config.listenPort
                        address = config.address
                        mtu = config.mtu
                        postUp = config.postUp
                        postDown = config.postDown
                        enabled = config.enabled
                        interfaceName = config.interfaceName
                    }
                    wireGuardConfigRepository.save(existing)
                } else {
                    wireGuardConfigRepository.save(config)
                }

                // Restore peers
                peerRepository.deleteByConfigId(existing.id!!)
                backupData.peers.forEach { peer ->
                    peer.config = existing
                    peerRepository.save(peer)
                }
            }

            // Restore proxy configs
            proxyConfigRepository.deleteAll()
            backupData.proxyConfigs.forEach { proxy ->
                proxyConfigRepository.save(proxy)
            }

            // Restore routing rules
            routingRuleRepository.deleteAll()
            backupData.routingRules.forEach { rule ->
                routingRuleRepository.save(rule)
            }

            logger.info { "Restored backup: ${backup.name}" }
        } catch (e: Exception) {
            logger.error(e) { "Failed to restore backup" }
            throw ru.sersb.wgsingbox.model.exception.ServiceException("Failed to restore backup: ${e.message}")
        }
    }

    fun deleteBackup(id: Long) {
        configBackupRepository.deleteById(id)
        logger.info { "Deleted backup: $id" }
    }
}
