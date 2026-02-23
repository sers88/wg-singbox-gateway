package ru.sersb.wgsingbox.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.sersb.wgsingbox.model.entity.RoutingRule

@Repository
interface RoutingRuleRepository : JpaRepository<RoutingRule, Long> {
    fun findByEnabledTrueOrderByPriorityAsc(): List<RoutingRule>
    fun findByTypeAndEnabledTrueOrderByPriorityAsc(type: ru.sersb.wgsingbox.model.enum.RuleType): List<RoutingRule>
    fun findAllByOrderByPriorityAsc(): List<RoutingRule>
}
