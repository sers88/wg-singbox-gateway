package ru.sersb.wgsingbox.model.dto.response

import ru.sersb.wgsingbox.model.entity.ProxyConfig

data class ProxyConfigResponse(
    val id: Long?,
    val type: String,
    val server: String,
    val serverPort: Int,
    val password: String?,
    val uuid: String?,
    val serverName: String?,
    val insecure: Boolean,
    val network: String,
    val flow: String?,
    val alterId: Int,
    val security: String,
    val method: String,
    val enabled: Boolean,
    val priority: Int,
    val createdAt: String,
    val updatedAt: String
) {
    companion object {
        fun fromEntity(entity: ProxyConfig): ProxyConfigResponse {
            return ProxyConfigResponse(
                id = entity.id,
                type = entity.type.name,
                server = entity.server,
                serverPort = entity.serverPort,
                password = entity.password,
                uuid = entity.uuid,
                serverName = entity.serverName,
                insecure = entity.insecure,
                network = entity.network.name,
                flow = entity.flow,
                alterId = entity.alterId,
                security = entity.security,
                method = entity.method.name,
                enabled = entity.enabled,
                priority = entity.priority,
                createdAt = entity.createdAt.toString(),
                updatedAt = entity.updatedAt.toString()
            )
        }
    }
}
