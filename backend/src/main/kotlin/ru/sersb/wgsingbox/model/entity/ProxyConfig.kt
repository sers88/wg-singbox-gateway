package ru.sersb.wgsingbox.model.entity

import jakarta.persistence.*
import ru.sersb.wgsingbox.model.enum.EncryptionMethod
import ru.sersb.wgsingbox.model.enum.NetworkType
import ru.sersb.wgsingbox.model.enum.ProxyType
import java.time.LocalDateTime

@Entity
@Table(name = "proxy_config")
data class ProxyConfig(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var type: ProxyType = ProxyType.TROJAN,

    @Column(nullable = false)
    var server: String = "",

    @Column(name = "server_port", nullable = false)
    var serverPort: Int = 443,

    @Column
    var password: String? = null,

    @Column
    var uuid: String? = null,

    @Column(name = "server_name")
    var serverName: String? = null,

    @Column(nullable = false)
    var insecure: Boolean = false,

    @Enumerated(EnumType.STRING)
    var network: NetworkType = NetworkType.TCP,

    @Column
    var flow: String? = null,

    @Column(name = "alter_id")
    var alterId: Int = 0,

    @Column
    var security: String = "AUTO",

    @Enumerated(EnumType.STRING)
    var method: EncryptionMethod = EncryptionMethod.AES_256_GCM,

    @Column(nullable = false)
    var enabled: Boolean = true,

    @Column(nullable = false)
    var priority: Int = 1,

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
