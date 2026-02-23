package ru.sersb.wgsingbox.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.sersb.wgsingbox.model.entity.Peer

@Repository
interface PeerRepository : JpaRepository<Peer, Long> {
    fun findByConfigIdAndEnabledTrueOrderByCreatedAtAsc(configId: Long): List<Peer>
    fun findByConfigIdOrderByCreatedAtAsc(configId: Long): List<Peer>
    fun findByPublicKey(publicKey: String): Peer?
    fun deleteByConfigId(configId: Long)
}
