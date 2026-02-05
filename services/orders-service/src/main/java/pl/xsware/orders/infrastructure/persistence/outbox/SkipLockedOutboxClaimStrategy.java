package pl.xsware.orders.infrastructure.persistence.outbox;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import pl.xsware.orders.application.outbox.OutboxClaimStrategy;

import java.time.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
@ConditionalOnProperty(
    name = "orders.outbox.claim-mode",
    havingValue = "SKIP_LOCKED",
    matchIfMissing = true
)
public class SkipLockedOutboxClaimStrategy implements OutboxClaimStrategy {

    private final NamedParameterJdbcTemplate jdbc;
    private final Clock clock;

    @Override
    @Transactional
    public List<UUID> claimNextBatch(int batchSize, Duration lockTimeout, String lockedBy) {
        Instant nowInstant = Instant.now(clock);
        Instant lockExpiredBeforeInstant = nowInstant.minus(lockTimeout);

        OffsetDateTime now = OffsetDateTime.ofInstant(nowInstant, ZoneOffset.UTC);
        OffsetDateTime lockExpiredBefore = OffsetDateTime.ofInstant(lockExpiredBeforeInstant, ZoneOffset.UTC);

        String sql = """
            WITH candidates AS (
                SELECT id
                FROM outbox_messages
                WHERE processed_at IS NULL
                  AND (next_attempt_at IS NULL OR next_attempt_at <= :now)
                  AND (locked_at IS NULL OR locked_at < :lockExpiredBefore)
                ORDER BY occurred_at
                FOR UPDATE SKIP LOCKED
                LIMIT :batchSize
            )
            UPDATE outbox_messages om
            SET locked_at = :now,
                locked_by = :lockedBy
            FROM candidates c
            WHERE om.id = c.id
            RETURNING om.id
            """;

        Map<String, Object> params = new HashMap<>();
        params.put("now", now);
        params.put("lockExpiredBefore", lockExpiredBefore);
        params.put("lockedBy", lockedBy);
        params.put("batchSize", batchSize);

        return jdbc.query(sql, params, (rs, rowNum) -> UUID.fromString(rs.getString("id")));
    }

}
