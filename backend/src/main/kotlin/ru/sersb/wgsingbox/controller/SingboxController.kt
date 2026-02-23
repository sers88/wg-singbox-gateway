package ru.sersb.wgsingbox.controller

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import ru.sersb.wgsingbox.model.dto.response.ApiResponse
import ru.sersb.wgsingbox.model.dto.response.SingboxStatusResponse

@RestController
@RequestMapping("/api/singbox")
class SingboxController(
    private val singboxService: ru.sersb.wgsingbox.service.SingboxService
) {
    private val logger = KotlinLogging.logger {}

    @GetMapping("/status")
    fun getStatus(): ResponseEntity<ApiResponse<SingboxStatusResponse>> {
        val status = singboxService.getStatus()
        return ResponseEntity.ok(ApiResponse.success(status))
    }

    @PostMapping("/start")
    fun start(): ResponseEntity<ApiResponse<SingboxStatusResponse>> {
        val status = singboxService.start()
        return ResponseEntity.ok(ApiResponse.success(status, "Singbox started"))
    }

    @PostMapping("/stop")
    fun stop(): ResponseEntity<ApiResponse<SingboxStatusResponse>> {
        val status = singboxService.stop()
        return ResponseEntity.ok(ApiResponse.success(status, "Singbox stopped"))
    }

    @PostMapping("/restart")
    fun restart(): ResponseEntity<ApiResponse<SingboxStatusResponse>> {
        val status = singboxService.restart()
        return ResponseEntity.ok(ApiResponse.success(status, "Singbox restarted"))
    }
}
