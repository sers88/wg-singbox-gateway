package ru.sersb.wgsingbox.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.sersb.wgsingbox.model.entity.AuditLog

@Repository
interface AuditLogRepository : JpaRepository<AuditLog, Long> {
    fun findByUserIdOrderByCreatedAtDesc(userId: Long): List<AuditLog>
    fun findByEntityTypeOrderByCreatedAtDesc(entityType: String): List<AuditLog>
    fun findByEntityTypeAndEntityIdOrderByCreatedAtDesc(entityType: String, entityId: Long): List<AuditLog>
    fun findAllByOrderByCreatedAtDesc(limit: Int = 100): List<AuditLog>
}
