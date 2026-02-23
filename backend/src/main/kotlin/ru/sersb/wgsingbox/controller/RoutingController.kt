package ru.sersb.wgsingbox.controller

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import ru.sersb.wgsingbox.model.dto.request.RoutingRuleRequest
import ru.sersb.wgsingbox.model.dto.response.ApiResponse
import ru.sersb.wgsingbox.model.dto.response.RoutingRuleResponse

@RestController
@RequestMapping("/api/routing")
class RoutingController(
    private val routingRuleService: ru.sersb.wgsingbox.service.RoutingRuleService
) {
    private val logger = KotlinLogging.logger {}

    @GetMapping("/rules")
    fun getAll(): ResponseEntity<ApiResponse<List<RoutingRuleResponse>>> {
        val rules = routingRuleService.getAll()
        return ResponseEntity.ok(ApiResponse.success(rules))
    }

    @GetMapping("/rules/{id}")
    fun getById(@PathVariable id: Long): ResponseEntity<ApiResponse<RoutingRuleResponse>> {
        val rule = routingRuleService.getById(id)
        return ResponseEntity.ok(ApiResponse.success(rule))
    }

    @PostMapping("/rules")
    fun create(@Valid @RequestBody request: RoutingRuleRequest): ResponseEntity<ApiResponse<RoutingRuleResponse>> {
        val rule = routingRuleService.create(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(rule, "Routing rule created"))
    }

    @PutMapping("/rules/{id}")
    fun update(
        @PathVariable id: Long,
        @Valid @RequestBody request: RoutingRuleRequest
    ): ResponseEntity<ApiResponse<RoutingRuleResponse>> {
        val rule = routingRuleService.update(id, request)
        return ResponseEntity.ok(ApiResponse.success(rule, "Routing rule updated"))
    }

    @DeleteMapping("/rules/{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<ApiResponse<Unit>> {
        routingRuleService.delete(id)
        return ResponseEntity.ok(ApiResponse.success(Unit, "Routing rule deleted"))
    }

    @PostMapping("/rules/{id}/toggle")
    fun toggle(@PathVariable id: Long): ResponseEntity<ApiResponse<RoutingRuleResponse>> {
        val rule = routingRuleService.toggle(id)
        return ResponseEntity.ok(ApiResponse.success(rule))
    }

    @PostMapping("/rules/reorder")
    fun reorder(@RequestBody requests: List<RoutingRuleRequest>): ResponseEntity<ApiResponse<List<RoutingRuleResponse>>> {
        val rules = routingRuleService.reorder(requests)
        return ResponseEntity.ok(ApiResponse.success(rules, "Routing rules reordered"))
    }

    @GetMapping("/rules/types")
    fun getTypes(): ResponseEntity<ApiResponse<List<Map<String, String>>>> {
        val types = listOf(
            mapOf("value" to "DOMAIN", "label" to "Domain"),
            mapOf("value" to "IP_CIDR", "label" to "IP CIDR"),
            mapOf("value" to "GEOSITE", "label" to "GeoSite")
        )
        return ResponseEntity.ok(ApiResponse.success(types))
    }

    @GetMapping("/rules/geosite-categories")
    fun getGeoSiteCategories(): ResponseEntity<ApiResponse<List<String>>> {
        val categories = routingRuleService.getAvailableGeoSiteCategories()
        return ResponseEntity.ok(ApiResponse.success(categories))
    }
}

@RestControllerAdvice
class RoutingControllerAdvice {
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
