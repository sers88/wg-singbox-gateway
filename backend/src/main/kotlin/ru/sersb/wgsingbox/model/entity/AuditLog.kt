package ru.sersb.wgsingbox.model.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "audit_log")
data class AuditLog(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    var user: User? = null,

    @Column(nullable = false)
    var action: String = "",

    @Column(name = "entity_type", nullable = false)
    var entityType: String = "",

    @Column(name = "entity_id")
    var entityId: Long? = null,

    @Column(columnDefinition = "TEXT")
    var details: String? = null,

    @Column(name = "ip_address")
    var ipAddress: String? = null,

    @Column(name = "created_at", updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)
