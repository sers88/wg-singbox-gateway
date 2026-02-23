package ru.sersb.wgsingbox.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.sersb.wgsingbox.model.entity.WireGuardConfig

@Repository
interface WireGuardConfigRepository : JpaRepository<WireGuardConfig, Long> {
    fun findFirstByOrderByCreatedAtAsc(): WireGuardConfig?
    fun findByEnabledTrue(): WireGuardConfig?
}
