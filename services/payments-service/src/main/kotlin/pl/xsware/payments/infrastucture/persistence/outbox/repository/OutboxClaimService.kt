package pl.xsware.payments.infrastucture.persistence.outbox.repository

import jakarta.persistence.EntityManager
import jakarta.persistence.LockModeType
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import pl.xsware.payments.infrastucture.persistence.outbox.entity.OutboxEntity
import java.time.Clock
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class OutboxClaimService(
    private val em: EntityManager,
    private val clock: Clock
) {

    @Transactional
    fun claimBatch(limit: Int, lockSeconds: Long = 30): List<OutboxEntity> {
        val now = Instant.now(clock)
        val lockUntil = now.plus(lockSeconds, ChronoUnit.SECONDS)

        val items = em.createQuery(
            """
            SELECT o FROM OutboxEntity o
            WHERE o.status IN ('PENDING','FAILED')
              AND (o.lockedUntil IS NULL OR o.lockedUntil < :now)
            ORDER BY o.createdAt ASC
            """.trimIndent(),
            OutboxEntity::class.java
        )
            .setParameter("now", now)
            .setMaxResults(limit)
            .setLockMode(LockModeType.PESSIMISTIC_WRITE)
            .resultList

        items.forEach { it.lockedUntil = lockUntil }
        return items
    }
}
