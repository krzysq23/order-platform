package pl.xsware.inventory.infrastructure.persistance.outbox.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.xsware.inventory.infrastructure.persistance.outbox.entity.OutboxMessageEntity;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface OutboxMessageJpaRepository extends JpaRepository<OutboxMessageEntity, UUID> {

    @Query("""
        select m
        from OutboxMessageEntity m
        where m.processedAt is null
          and (m.nextAttemptAt is null or m.nextAttemptAt <= :now)
          and m.lockedAt is null
        order by m.createdAt asc
        """)
    List<OutboxMessageEntity> findReadyUnlocked(@Param("now") Instant now);

    @Transactional
    @Modifying
    @Query(value = """
        update outbox_messages
        set locked_at = :now,
            locked_by = :lockedBy
        where id in (
            select id
            from outbox_messages
            where processed_at is null
              and (next_attempt_at is null or next_attempt_at <= :now)
              and locked_at is null
              and (:eventType is null or event_type = :eventType)
            order by created_at asc
            for update skip locked
            limit :limit
        )
        returning *
        """, nativeQuery = true)
    List<OutboxMessageEntity> claimBatch(
        @Param("now") Instant now,
        @Param("lockedBy") String lockedBy,
        @Param("limit") int limit,
        @Param("eventType") String eventType
    );

    @Transactional
    @Modifying
    @Query("""
        update OutboxMessageEntity m
        set m.processedAt = :processedAt,
            m.lockedAt = null,
            m.lockedBy = null,
            m.lastError = null
        where m.id = :id
        """)
    int markProcessed(@Param("id") UUID id, @Param("processedAt") Instant processedAt);

    @Transactional
    @Modifying
    @Query("""
        update OutboxMessageEntity m
        set m.attempts = m.attempts + 1,
            m.nextAttemptAt = :nextAttemptAt,
            m.lastError = :error,
            m.lockedAt = null,
            m.lockedBy = null
        where m.id = :id
        """)
    int markFailed(
        @Param("id") UUID id,
        @Param("nextAttemptAt") Instant nextAttemptAt,
        @Param("error") String error
    );
}
