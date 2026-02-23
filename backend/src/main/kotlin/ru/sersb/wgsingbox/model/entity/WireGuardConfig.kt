package ru.sersb.wgsingbox.model.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "wireguard_config")
data class WireGuardConfig(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, unique = true)
    var privateKey: String = "",

    @Column(nullable = false)
    var publicKey: String = "",

    @Column(name = "listen_port", nullable = false)
    var listenPort: Int = 51820,

    @Column(nullable = false)
    var address: String = "10.0.0.1/24",

    @Column(nullable = false)
    var mtu: Int = 1280,

    @Column(name = "post_up", columnDefinition = "TEXT")
    var postUp: String? = null,

    @Column(name = "post_down", columnDefinition = "TEXT")
    var postDown: String? = null,

    @Column(nullable = false)
    var enabled: Boolean = true,

    @Column(name = "interface_name")
    var interfaceName: String = "wg0",

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
