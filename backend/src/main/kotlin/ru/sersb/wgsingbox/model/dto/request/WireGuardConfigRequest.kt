package ru.sersb.wgsingbox.model.dto.request

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

data class WireGuardConfigRequest(
    @field:NotBlank(message = "Private key is required")
    var privateKey: String,

    @field:NotBlank(message = "Public key is required")
    var publicKey: String,

    @field:Min(value = 1, message = "Listen port must be at least 1")
    var listenPort: Int = 51820,

    @field:NotBlank(message = "Address is required")
    var address: String = "10.0.0.1/24",

    @field:Min(value = 576, message = "MTU must be at least 576")
    var mtu: Int = 1280,

    var postUp: String? = null,

    var postDown: String? = null,

    var enabled: Boolean = true,

    var interfaceName: String = "wg0"
)
