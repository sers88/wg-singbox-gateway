package ru.sersb.wgsingbox.model.entity

import jakarta.persistence.*
import ru.sersb.wgsingbox.model.enum.RuleType
import java.time.LocalDateTime

@Entity
@Table(name = "routing_rule")
data class RoutingRule(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var type: RuleType = RuleType.DOMAIN,

    @Column(nullable = false, columnDefinition = "TEXT")
    var value: String = "",

    @Column(name = "outbound_tag", nullable = false)
    var outboundTag: String = "direct",

    @Column(nullable = false)
    var enabled: Boolean = true,

    @Column(nullable = false)
    var priority: Int = 100,

    @Column(columnDefinition = "TEXT")
    var description: String? = null,

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
