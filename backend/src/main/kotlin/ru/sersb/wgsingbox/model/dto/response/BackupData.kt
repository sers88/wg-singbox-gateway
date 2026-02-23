package ru.sersb.wgsingbox.model.dto.response

import ru.sersb.wgsingbox.model.entity.ProxyConfig
import ru.sersb.wgsingbox.model.entity.RoutingRule
import ru.sersb.wgsingbox.model.entity.WireGuardConfig
import ru.sersb.wgsingbox.model.entity.Peer

data class BackupData(
    val wireGuardConfig: WireGuardConfig?,
    val peers: List<Peer>,
    val proxyConfigs: List<ProxyConfig>,
    val routingRules: List<RoutingRule>
)
