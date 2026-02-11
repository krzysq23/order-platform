package pl.xsware.orders.infrastructure.persistence.outbox;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import pl.xsware.orders.application.event.PaymentRequestedEvent;
import pl.xsware.orders.application.event.ReserveStockRequestedEvent;
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

        /*
            w jednym zapytaniu wybieramy rekordy do przetworzenia,
            omijamy te aktualnie zablokowane przez inne instancje
            i od razu oznacza wybrane jako zajęte.
            - FOR UPDATE zakłada row lock na wybrane wiersze,
              tak żeby inne transakcje nie mogły ich równolegle modyfikować w konfliktujący sposób.
            - SKIP LOCKED mówi: jeśli jakiś wiersz jest już zablokowany przez inną transakcję,
              nie czekaj, tylko go pomiń i wybierz następny.
         */
        String sql = """
            WITH candidates AS (
                SELECT id
                FROM outbox_messages
                WHERE processed_at IS NULL
                  AND event_type IN (:eventTypes)
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
        params.put("eventTypes", List.of(PaymentRequestedEvent.TYPE, ReserveStockRequestedEvent.TYPE));

        return jdbc.query(sql, params, (rs, rowNum) -> UUID.fromString(rs.getString("id")));
    }

}
