package ru.sersb.wgsingbox.model.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "peer")
data class Peer(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "config_id", nullable = false)
    var config: WireGuardConfig? = null,

    @Column(nullable = false, unique = true)
    var publicKey: String = "",

    @Column(name = "preshared_key")
    var presharedKey: String? = null,

    @Column(name = "allowed_ips", nullable = false)
    var allowedIps: String = "",

    @Column(nullable = false)
    var name: String = "",

    @Column(name = "device_type")
    var deviceType: String? = null,

    @Column(name = "endpoint_ip")
    var endpointIp: String? = null,

    @Column(name = "endpoint_port")
    var endpointPort: Int? = null,

    @Column(name = "persistent_keepalive")
    var persistentKeepalive: Int = 25,

    @Column(name = "last_handshake")
    var lastHandshake: LocalDateTime? = null,

    @Column(name = "transfer_rx")
    var transferRx: Long = 0,

    @Column(name = "transfer_tx")
    var transferTx: Long = 0,

    @Column(nullable = false)
    var enabled: Boolean = true,

    @Column(name = "created_at", updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    @PreUpdate
    fun onUpdate() {
        updatedAt = LocalDateTime.now()
    }
}
