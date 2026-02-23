package ru.sersb.wgsingbox.model.dto.request

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import ru.sersb.wgsingbox.model.enum.EncryptionMethod
import ru.sersb.wgsingbox.model.enum.NetworkType
import ru.sersb.wgsingbox.model.enum.ProxyType

data class ProxyConfigRequest(
    @field:NotNull(message = "Proxy type is required")
    var type: ProxyType,

    @field:NotBlank(message = "Server is required")
    var server: String,

    @field:Min(value = 1, message = "Server port must be at least 1")
    @field:NotNull(message = "Server port is required")
    var serverPort: Int,

    var password: String? = null,

    var uuid: String? = null,

    var serverName: String? = null,

    var insecure: Boolean = false,

    var network: NetworkType = NetworkType.TCP,

    var flow: String? = null,

    var alterId: Int = 0,

    var security: String = "AUTO",

    var method: EncryptionMethod = EncryptionMethod.AES_256_GCM,

    var enabled: Boolean = true,

    var priority: Int = 1
)
