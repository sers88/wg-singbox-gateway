package ru.sersb.wgsingbox.service

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service
import ru.sersb.wgsingbox.integration.wireguard.WireGuardConfigGenerator
import ru.sersb.wgsingbox.integration.wireguard.WireGuardManager
import ru.sersb.wgsingbox.model.dto.request.PeerRequest
import ru.sersb.wgsingbox.model.dto.request.WireGuardConfigRequest
import ru.sersb.wgsingbox.model.dto.response.PeerResponse
import ru.sersb.wgsingbox.model.dto.response.WireGuardConfigResponse
import ru.sersb.wgsingbox.model.dto.response.WireGuardStatusResponse
import ru.sersb.wgsingbox.model.exception.ResourceNotFoundException
import ru.sersb.wgsingbox.model.exception.ServiceException
import ru.sersb.wgsingbox.model.exception.ValidationException
import ru.sersb.wgsingbox.model.entity.Peer
import ru.sersb.wgsingbox.model.entity.WireGuardConfig
import ru.sersb.wgsingbox.model.enum.ServiceStatus
import ru.sersb.wgsingbox.repository.PeerRepository
import ru.sersb.wgsingbox.repository.WireGuardConfigRepository

@Service
class WireGuardService(
    private val wireGuardConfigRepository: WireGuardConfigRepository,
    private val peerRepository: PeerRepository,
    private val wireGuardManager: WireGuardManager,
    private val configGenerator: WireGuardConfigGenerator
) {
    private val logger = KotlinLogging.logger {}

    fun getConfig(): WireGuardConfigResponse {
        val config = wireGuardConfigRepository.findByEnabledTrue()
            ?: wireGuardConfigRepository.findFirstByOrderByCreatedAtAsc()
            ?: throw ResourceNotFoundException("WireGuard configuration not found")

        val status = runBlocking { wireGuardManager.getStatus() }

        return WireGuardConfigResponse(
            id = config.id,
            privateKey = config.privateKey,
            publicKey = config.publicKey,
            listenPort = config.listenPort,
            address = config.address,
            mtu = config.mtu,
            postUp = config.postUp,
            postDown = config.postDown,
            enabled = config.enabled,
            interfaceName = config.interfaceName,
            status = status,
            createdAt = config.createdAt.toString(),
            updatedAt = config.updatedAt.toString()
        )
    }

    fun updateConfig(request: WireGuardConfigRequest): WireGuardConfigResponse {
        val config = wireGuardConfigRepository.findFirstByOrderByCreatedAtAsc()
            ?: WireGuardConfig().apply {
                this.enabled = false
            }

        config.apply {
            privateKey = request.privateKey
            publicKey = request.publicKey
            listenPort = request.listenPort
            address = request.address
            mtu = request.mtu
            postUp = request.postUp
            postDown = request.postDown
            enabled = request.enabled
            interfaceName = request.interfaceName
        }

        val savedConfig = wireGuardConfigRepository.save(config)

        // If enabled, restart interface
        if (savedConfig.enabled) {
            restartInterface()
        } else {
            stopInterface()
        }

        logger.info { "WireGuard configuration updated" }

        val status = runBlocking { wireGuardManager.getStatus() }

        return WireGuardConfigResponse(
            id = savedConfig.id,
            privateKey = savedConfig.privateKey,
            publicKey = savedConfig.publicKey,
            listenPort = savedConfig.listenPort,
            address = savedConfig.address,
            mtu = savedConfig.mtu,
            postUp = savedConfig.postUp,
            postDown = savedConfig.postDown,
            enabled = savedConfig.enabled,
            interfaceName = savedConfig.interfaceName,
            status = status,
            createdAt = savedConfig.createdAt.toString(),
            updatedAt = savedConfig.updatedAt.toString()
        )
    }

    fun getStatus(): WireGuardStatusResponse {
        val config = wireGuardConfigRepository.findByEnabledTrue()
            ?: wireGuardConfigRepository.findFirstByOrderByCreatedAtAsc()
            ?: throw ResourceNotFoundException("WireGuard configuration not found")

        val (status, wgStatus) = runBlocking { wireGuardManager.getDetailedStatus() }

        val peerStatsList = wgStatus?.peers?.map { wgPeer ->
            ru.sersb.wgsingbox.model.dto.response.PeerStats(
                publicKey = wgPeer.publicKey,
                endpoint = wgPeer.endpoint,
                allowedIps = wgPeer.allowedIps,
                latestHandshake = wgPeer.latestHandshake,
                transferRx = wgPeer.transferRx,
                transferTx = wgPeer.transferTx
            )
        } ?: emptyList()

        return WireGuardStatusResponse(
            status = status,
            interfaceName = config.interfaceName,
            publicKey = config.publicKey,
            listenPort = config.listenPort,
            peers = peerStatsList,
            uptime = null
        )
    }

    fun startInterface(): WireGuardStatusResponse {
        runBlocking { wireGuardManager.startInterface() }
        return getStatus()
    }

    fun stopInterface(): WireGuardStatusResponse {
        runBlocking { wireGuardManager.stopInterface() }
        return getStatus()
    }

    fun restartInterface(): WireGuardStatusResponse {
        runBlocking { wireGuardManager.restartInterface() }
        return getStatus()
    }

    fun getPeers(): List<PeerResponse> {
        val config = wireGuardConfigRepository.findFirstByOrderByCreatedAtAsc()
            ?: throw ResourceNotFoundException("WireGuard configuration not found")

        return peerRepository.findByConfigIdOrderByCreatedAtAsc(config.id!!)
            .map { PeerResponse.fromEntity(it) }
    }

    fun getPeer(id: Long): PeerResponse {
        val peer = peerRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Peer not found") }
        return PeerResponse.fromEntity(peer)
    }

    fun createPeer(request: PeerRequest): PeerResponse {
        val config = wireGuardConfigRepository.findFirstByOrderByCreatedAtAsc()
            ?: throw ResourceNotFoundException("WireGuard configuration not found")

        if (peerRepository.findByPublicKey(request.publicKey) != null) {
            throw ValidationException("Peer with this public key already exists")
        }

        val peer = Peer(
            config = config,
            publicKey = request.publicKey,
            presharedKey = request.presharedKey,
            allowedIps = request.allowedIps,
            name = request.name,
            deviceType = request.deviceType,
            endpointIp = request.endpointIp,
            endpointPort = request.endpointPort,
            persistentKeepalive = request.persistentKeepalive,
            enabled = request.enabled
        )

        val savedPeer = peerRepository.save(peer)

        // Restart interface to apply new peer
        if (config.enabled) {
            restartInterface()
        }

        logger.info { "Created peer: ${savedPeer.name}" }

        return PeerResponse.fromEntity(savedPeer)
    }

    fun updatePeer(id: Long, request: PeerRequest): PeerResponse {
        val peer = peerRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Peer not found") }

        // Check if public key is changed and conflicts with another peer
        if (peer.publicKey != request.publicKey) {
            if (peerRepository.findByPublicKey(request.publicKey) != null) {
                throw ValidationException("Peer with this public key already exists")
            }
        }

        peer.apply {
            publicKey = request.publicKey
            presharedKey = request.presharedKey
            allowedIps = request.allowedIps
            name = request.name
            deviceType = request.deviceType
            endpointIp = request.endpointIp
            endpointPort = request.endpointPort
            persistentKeepalive = request.persistentKeepalive
            enabled = request.enabled
        }

        val savedPeer = peerRepository.save(peer)

        // Restart interface to apply changes
        if (peer.config?.enabled == true) {
            restartInterface()
        }

        logger.info { "Updated peer: ${savedPeer.name}" }

        return PeerResponse.fromEntity(savedPeer)
    }

    fun deletePeer(id: Long) {
        val peer = peerRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Peer not found") }

        val config = peer.config

        peerRepository.deleteById(id)

        // Restart interface to apply changes
        if (config?.enabled == true) {
            restartInterface()
        }

        logger.info { "Deleted peer: ${peer.name}" }
    }

    fun togglePeer(id: Long): PeerResponse {
        val peer = peerRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Peer not found") }

        peer.enabled = !peer.enabled
        val savedPeer = peerRepository.save(peer)

        // Restart interface to apply changes
        if (peer.config?.enabled == true) {
            restartInterface()
        }

        logger.info { "Toggled peer ${peer.name} to ${if (savedPeer.enabled) "enabled" else "disabled"}" }

        return PeerResponse.fromEntity(savedPeer)
    }

    fun getPeerQRCode(id: Long, serverEndpoint: String?): String {
        val peer = peerRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Peer not found") }

        val config = wireGuardConfigRepository.findByEnabledTrue()
            ?: wireGuardConfigRepository.findFirstByOrderByCreatedAtAsc()
            ?: throw ResourceNotFoundException("WireGuard configuration not found")

        // Generate client config
        val clientConfig = configGenerator.generatePeerClientConfig(config, peer, serverEndpoint)

        logger.info { "Generated QR code for peer: ${peer.name}" }

        return clientConfig
    }
}
