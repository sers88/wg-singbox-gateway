package ru.sersb.wgsingbox.model.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import ru.sersb.wgsingbox.model.enum.RuleType

data class RoutingRuleRequest(
    var id: Long? = null,

    @field:NotNull(message = "Rule type is required")
    var type: RuleType,

    @field:NotBlank(message = "Value is required")
    var value: String,

    @field:NotBlank(message = "Outbound tag is required")
    var outboundTag: String,

    var enabled: Boolean = true,

    var priority: Int = 100,

    var description: String? = null
)
