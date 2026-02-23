package ru.sersb.wgsingbox.model.dto.request

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank

data class PeerRequest(
    @field:NotBlank(message = "Public key is required")
    var publicKey: String,

    var presharedKey: String? = null,

    @field:NotBlank(message = "Allowed IPs is required")
    var allowedIps: String,

    @field:NotBlank(message = "Name is required")
    var name: String,

    var deviceType: String? = null,

    var endpointIp: String? = null,

    var endpointPort: Int? = null,

    @field:Min(value = 0, message = "Persistent keepalive must be non-negative")
    var persistentKeepalive: Int = 25,

    var enabled: Boolean = true
)
