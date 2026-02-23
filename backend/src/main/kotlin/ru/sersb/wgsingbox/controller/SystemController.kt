package ru.sersb.wgsingbox.controller

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.validation.constraints.NotBlank
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import ru.sersb.wgsingbox.model.dto.response.ApiResponse
import ru.sersb.wgsingbox.model.dto.response.ConfigBackupResponse
import ru.sersb.wgsingbox.model.dto.response.SystemInfoResponse
import ru.sersb.wgsingbox.model.dto.response.SystemStatusResponse

data class CreateBackupRequest(
    @field:NotBlank(message = "Name is required")
    val name: String,
    val description: String? = null
)

@RestController
@RequestMapping("/api/system")
class SystemController(
    private val systemMonitoringService: ru.sersb.wgsingbox.service.SystemMonitoringService,
    private val backupService: ru.sersb.wgsingbox.service.BackupService
) {
    private val logger = KotlinLogging.logger {}

    @GetMapping("/status")
    fun getStatus(): ResponseEntity<ApiResponse<SystemStatusResponse>> {
        val status = systemMonitoringService.getStatus()
        return ResponseEntity.ok(ApiResponse.success(status))
    }

    @GetMapping("/info")
    fun getInfo(): ResponseEntity<ApiResponse<SystemInfoResponse>> {
        val info = systemMonitoringService.getSystemInfo()
        return ResponseEntity.ok(ApiResponse.success(info))
    }

    @GetMapping("/stats")
    fun getStats(): ResponseEntity<ApiResponse<Map<String, Any>>> {
        val stats = mapOf(
            "websocket" to systemMonitoringService.getWebSocketStats(),
            "system" to mapOf(
                "uptime" to systemMonitoringService.getStatus().uptime,
                "version" to "1.0.0"
            )
        )
        return ResponseEntity.ok(ApiResponse.success(stats))
    }

    @PostMapping("/backup")
    fun createBackup(@RequestBody request: CreateBackupRequest): ResponseEntity<ApiResponse<ConfigBackupResponse>> {
        val backup = backupService.createBackup(request.name, request.description)
        return ResponseEntity.ok(ApiResponse.success(backup, "Backup created successfully"))
    }

    @GetMapping("/backups")
    fun getBackups(): ResponseEntity<ApiResponse<List<ConfigBackupResponse>>> {
        val backups = backupService.getBackups()
        return ResponseEntity.ok(ApiResponse.success(backups))
    }

    @PostMapping("/backups/{id}/restore")
    fun restoreBackup(@PathVariable id: Long): ResponseEntity<ApiResponse<Unit>> {
        backupService.restoreBackup(id)
        return ResponseEntity.ok(ApiResponse.success(Unit, "Backup restored successfully"))
    }

    @DeleteMapping("/backups/{id}")
    fun deleteBackup(@PathVariable id: Long): ResponseEntity<ApiResponse<Unit>> {
        backupService.deleteBackup(id)
        return ResponseEntity.ok(ApiResponse.success(Unit, "Backup deleted successfully"))
    }
}

@RestControllerAdvice
class SystemControllerAdvice {
    private val logger = KotlinLogging.logger {}

    @ExceptionHandler(ru.sersb.wgsingbox.model.exception.ResourceNotFoundException::class)
    fun handleResourceNotFoundException(ex: ru.sersb.wgsingbox.model.exception.ResourceNotFoundException): ResponseEntity<ApiResponse<Unit>> {
        logger.warn { "Resource not found: ${ex.message}" }
        return ResponseEntity.status(404).body(ApiResponse.error(ex.message ?: "Resource not found"))
    }

    @ExceptionHandler(ru.sersb.wgsingbox.model.exception.ServiceException::class)
    fun handleServiceException(ex: ru.sersb.wgsingbox.model.exception.ServiceException): ResponseEntity<ApiResponse<Unit>> {
        logger.error(ex) { "Service error: ${ex.message}" }
        return ResponseEntity.status(500).body(ApiResponse.error(ex.message ?: "Service error"))
    }
}
