package ru.sersb.wgsingbox.model.dto.response

import ru.sersb.wgsingbox.model.enum.ServiceStatus

data class SingboxStatusResponse(
    val status: ServiceStatus,
    val activeProxy: ProxyConfigResponse?,
    val uptime: Long?,
    val memoryUsage: Long?
)
