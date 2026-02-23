package ru.sersb.wgsingbox.model.dto.response

import ru.sersb.wgsingbox.model.entity.RoutingRule

data class RoutingRuleResponse(
    val id: Long?,
    val type: String,
    val value: String,
    val outboundTag: String,
    val enabled: Boolean,
    val priority: Int,
    val description: String?,
    val createdAt: String,
    val updatedAt: String
) {
    companion object {
        fun fromEntity(entity: RoutingRule): RoutingRuleResponse {
            return RoutingRuleResponse(
                id = entity.id,
                type = entity.type.name,
                value = entity.value,
                outboundTag = entity.outboundTag,
                enabled = entity.enabled,
                priority = entity.priority,
                description = entity.description,
                createdAt = entity.createdAt.toString(),
                updatedAt = entity.updatedAt.toString()
            )
        }
    }
}
