package pl.xsware.payments.infrastucture.persistence.outbox.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import pl.xsware.payments.infrastucture.persistence.outbox.entity.OutboxEntity
import java.time.Instant
import java.util.UUID

interface OutboxJpaRepository : JpaRepository<OutboxEntity, UUID> {

    @Query(
        """
        SELECT o FROM OutboxEntity o
        WHERE o.status IN ('PENDING', 'FAILED')
          AND (o.lockedUntil IS NULL OR o.lockedUntil < :now)
        ORDER BY o.createdAt ASC
        """
    )
    fun findCandidates(@Param("now") now: Instant): List<OutboxEntity>
}
