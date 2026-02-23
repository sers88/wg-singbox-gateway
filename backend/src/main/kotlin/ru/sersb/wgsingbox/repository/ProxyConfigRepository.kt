package ru.sersb.wgsingbox.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.sersb.wgsingbox.model.entity.ProxyConfig

@Repository
interface ProxyConfigRepository : JpaRepository<ProxyConfig, Long> {
    fun findByEnabledTrueOrderByPriorityAsc(): List<ProxyConfig>
    fun findByTypeAndEnabledTrueOrderByPriorityAsc(type: ru.sersb.wgsingbox.model.enum.ProxyType): List<ProxyConfig>
    fun findAllByOrderByPriorityAsc(): List<ProxyConfig>
}
