package ru.sersb.wgsingbox.controller

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import ru.sersb.wgsingbox.model.dto.request.ProxyConfigRequest
import ru.sersb.wgsingbox.model.dto.response.ApiResponse
import ru.sersb.wgsingbox.model.dto.response.ProxyConfigResponse

@RestController
@RequestMapping("/api/proxy")
class ProxyController(
    private val proxyConfigService: ru.sersb.wgsingbox.service.ProxyConfigService
) {
    private val logger = KotlinLogging.logger {}

    @GetMapping("/configs")
    fun getAll(): ResponseEntity<ApiResponse<List<ProxyConfigResponse>>> {
        val configs = proxyConfigService.getAll()
        return ResponseEntity.ok(ApiResponse.success(configs))
    }

    @GetMapping("/configs/active")
    fun getActive(): ResponseEntity<ApiResponse<ProxyConfigResponse?>> {
        val configs = proxyConfigService.getAll()
        val active = configs.firstOrNull { it.enabled }
        return ResponseEntity.ok(ApiResponse.success(active))
    }

    @GetMapping("/configs/{id}")
    fun getById(@PathVariable id: Long): ResponseEntity<ApiResponse<ProxyConfigResponse>> {
        val config = proxyConfigService.getById(id)
        return ResponseEntity.ok(ApiResponse.success(config))
    }

    @PostMapping("/configs")
    fun create(@Valid @RequestBody request: ProxyConfigRequest): ResponseEntity<ApiResponse<ProxyConfigResponse>> {
        val config = proxyConfigService.create(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(config, "Proxy configuration created"))
    }

    @PutMapping("/configs/{id}")
    fun update(
        @PathVariable id: Long,
        @Valid @RequestBody request: ProxyConfigRequest
    ): ResponseEntity<ApiResponse<ProxyConfigResponse>> {
        val config = proxyConfigService.update(id, request)
        return ResponseEntity.ok(ApiResponse.success(config, "Proxy configuration updated"))
    }

    @DeleteMapping("/configs/{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<ApiResponse<Unit>> {
        proxyConfigService.delete(id)
        return ResponseEntity.ok(ApiResponse.success(Unit, "Proxy configuration deleted"))
    }

    @PostMapping("/configs/{id}/toggle")
    fun toggle(@PathVariable id: Long): ResponseEntity<ApiResponse<ProxyConfigResponse>> {
        val config = proxyConfigService.toggle(id)
        return ResponseEntity.ok(ApiResponse.success(config))
    }

    @PostMapping("/configs/{id}/active")
    fun setActive(@PathVariable id: Long): ResponseEntity<ApiResponse<ProxyConfigResponse>> {
        val config = proxyConfigService.setActive(id)
        return ResponseEntity.ok(ApiResponse.success(config, "Proxy set as active"))
    }

    @PostMapping("/configs/{id}/test")
    fun test(@PathVariable id: Long): ResponseEntity<ApiResponse<Map<String, Any>>> {
        val result = proxyConfigService.testConnection(id)
        return ResponseEntity.ok(ApiResponse.success(result))
    }
}

@RestControllerAdvice
class ProxyControllerAdvice {
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
}
