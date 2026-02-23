package ru.sersb.wgsingbox.model.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "config_backup")
data class ConfigBackup(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    var name: String = "",

    @Column(columnDefinition = "TEXT")
    var description: String? = null,

    @Column(nullable = false, columnDefinition = "TEXT")
    var data: String = "",

    @Column(name = "created_at", updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)
