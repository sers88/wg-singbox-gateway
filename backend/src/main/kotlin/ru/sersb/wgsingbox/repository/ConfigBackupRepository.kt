package ru.sersb.wgsingbox.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.sersb.wgsingbox.model.entity.ConfigBackup

@Repository
interface ConfigBackupRepository : JpaRepository<ConfigBackup, Long> {
    fun findAllByOrderByCreatedAtDesc(): List<ConfigBackup>
}
