package ru.sersb.wgsingbox.controller

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import ru.sersb.wgsingbox.model.dto.request.PeerRequest
import ru.sersb.wgsingbox.model.dto.request.WireGuardConfigRequest
import ru.sersb.wgsingbox.model.dto.response.ApiResponse
import ru.sersb.wgsingbox.model.dto.response.PeerResponse
import ru.sersb.wgsingbox.model.dto.response.WireGuardConfigResponse
import ru.sersb.wgsingbox.model.dto.response.WireGuardStatusResponse

@RestController
@RequestMapping("/api/wireguard")
class WireGuardController(
    private val wireGuardService: ru.sersb.wgsingbox.service.WireGuardService
) {
    private val logger = KotlinLogging.logger {}

    @GetMapping("/config")
    fun getConfig(): ResponseEntity<ApiResponse<WireGuardConfigResponse>> {
        val config = wireGuardService.getConfig()
        return ResponseEntity.ok(ApiResponse.success(config))
    }

    @PutMapping("/config")
    fun updateConfig(@Valid @RequestBody request: WireGuardConfigRequest): ResponseEntity<ApiResponse<WireGuardConfigResponse>> {
        val config = wireGuardService.updateConfig(request)
        return ResponseEntity.ok(ApiResponse.success(config, "Configuration updated successfully"))
    }

    @GetMapping("/status")
    fun getStatus(): ResponseEntity<ApiResponse<WireGuardStatusResponse>> {
        val status = wireGuardService.getStatus()
        return ResponseEntity.ok(ApiResponse.success(status))
    }

    @PostMapping("/start")
    fun startInterface(): ResponseEntity<ApiResponse<WireGuardStatusResponse>> {
        val status = wireGuardService.startInterface()
        return ResponseEntity.ok(ApiResponse.success(status, "WireGuard interface started"))
    }

    @PostMapping("/stop")
    fun stopInterface(): ResponseEntity<ApiResponse<WireGuardStatusResponse>> {
        val status = wireGuardService.stopInterface()
        return ResponseEntity.ok(ApiResponse.success(status, "WireGuard interface stopped"))
    }

    @PostMapping("/restart")
    fun restartInterface(): ResponseEntity<ApiResponse<WireGuardStatusResponse>> {
        val status = wireGuardService.restartInterface()
        return ResponseEntity.ok(ApiResponse.success(status, "WireGuard interface restarted"))
    }

    @GetMapping("/peers")
    fun getPeers(): ResponseEntity<ApiResponse<List<PeerResponse>>> {
        val peers = wireGuardService.getPeers()
        return ResponseEntity.ok(ApiResponse.success(peers))
    }

    @GetMapping("/peers/{id}")
    fun getPeer(@PathVariable id: Long): ResponseEntity<ApiResponse<PeerResponse>> {
        val peer = wireGuardService.getPeer(id)
        return ResponseEntity.ok(ApiResponse.success(peer))
    }

    @PostMapping("/peers")
    fun createPeer(@Valid @RequestBody request: PeerRequest): ResponseEntity<ApiResponse<PeerResponse>> {
        val peer = wireGuardService.createPeer(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(peer, "Peer created successfully"))
    }

    @PutMapping("/peers/{id}")
    fun updatePeer(
        @PathVariable id: Long,
        @Valid @RequestBody request: PeerRequest
    ): ResponseEntity<ApiResponse<PeerResponse>> {
        val peer = wireGuardService.updatePeer(id, request)
        return ResponseEntity.ok(ApiResponse.success(peer, "Peer updated successfully"))
    }

    @DeleteMapping("/peers/{id}")
    fun deletePeer(@PathVariable id: Long): ResponseEntity<ApiResponse<Unit>> {
        wireGuardService.deletePeer(id)
        return ResponseEntity.ok(ApiResponse.success(Unit, "Peer deleted successfully"))
    }

    @PostMapping("/peers/{id}/toggle")
    fun togglePeer(@PathVariable id: Long): ResponseEntity<ApiResponse<PeerResponse>> {
        val peer = wireGuardService.togglePeer(id)
        return ResponseEntity.ok(ApiResponse.success(peer))
    }

    @GetMapping("/peers/{id}/qrcode", produces = [MediaType.TEXT_PLAIN_VALUE])
    fun getPeerQRCode(
        @PathVariable id: Long,
        @RequestParam(required = false) serverEndpoint: String?
    ): ResponseEntity<String> {
        val config = wireGuardService.getPeerQRCode(id, serverEndpoint)
        return ResponseEntity.ok(config)
    }
}

@RestControllerAdvice
class WireGuardControllerAdvice {
    private val logger = KotlinLogging.logger {}

    @ExceptionHandler(ru.sersb.wgsingbox.model.exception.ResourceNotFoundException::class)
    fun handleResourceNotFoundException(ex: ru.sersb.wgsingbox.model.exception.ResourceNotFoundException): ResponseEntity<ApiResponse<Unit>> {
        logger.warn { "Resource not found: ${ex.message}" }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(ex.message ?: "Resource not found"))
    }

    @ExceptionHandler(ru.sersb.wgsingbox.model.exception.ValidationException::class)
    fun handleValidationException(ex: ru.sersb.wgsingbox.model.exception.ValidationException): ResponseEntity<ApiResponse<Unit>> {
        logger.warn { "Validation error: ${ex.message}" }
        return ResponseEntity.badRequest().body(ApiResponse.error(ex.message ?: "Validation failed", ex.errors))
    }

    @ExceptionHandler(ru.sersb.wgsingbox.model.exception.ServiceException::class)
    fun handleServiceException(ex: ru.sersb.wgsingbox.model.exception.ServiceException): ResponseEntity<ApiResponse<Unit>> {
        logger.error(ex) { "Service error: ${ex.message}" }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(ex.message ?: "Service error"))
    }
}
