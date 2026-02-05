package pl.xsware.orders.infrastructure.persistence.outbox;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import pl.xsware.orders.application.outbox.OutboxClaimStrategy;

import java.time.*;
import java.util.*;

@Repository
@RequiredArgsConstructor
@ConditionalOnProperty(
    name = "orders.outbox.claim-mode",
    havingValue = "ADVISORY"
)
public class AdvisoryLockOutboxClaimStrategy implements OutboxClaimStrategy {

    private final NamedParameterJdbcTemplate jdbc;
    private final Clock clock;

    @Transactional
    @Override
    public List<UUID> claimNextBatch(int batchSize, Duration lockTimeout, String lockedBy) {

        Instant nowInstant = Instant.now(clock);
        Instant lockExpiredBeforeInstant = nowInstant.minus(lockTimeout);

        OffsetDateTime now = OffsetDateTime.ofInstant(nowInstant, ZoneOffset.UTC);
        OffsetDateTime lockExpiredBefore =
            OffsetDateTime.ofInstant(lockExpiredBeforeInstant, ZoneOffset.UTC);

        // Skanuje więcej wierszy niż wynosi batchSize, aby uwzględnić race condition i już zablokowane wiersze.
        int scanLimit = Math.max(batchSize * 5, batchSize);

        List<UUID> candidates = selectCandidateIds(now, lockExpiredBefore, scanLimit);
        if (candidates.isEmpty()) {
            return List.of();
        }

        // Non-blocking advisory locks aby uniknąć oczekiwania na zablokowane wiersze
        List<UUID> locked = new ArrayList<>(batchSize);
        for (UUID id : candidates) {
            if (tryAdvisoryLock(id) && locked.size() < batchSize) {
                locked.add(id);
            }
            if (locked.size() >= batchSize) break;
        }

        if (locked.isEmpty()) {
            return List.of();
        }

        // Dodatkowe warunki WHERE chronią przed races condition
        String updateSql = """
            UPDATE outbox_messages
            SET locked_at = :now,
                locked_by = :lockedBy
            WHERE id = ANY(:ids)
              AND processed_at IS NULL
              AND (next_attempt_at IS NULL OR next_attempt_at <= :now)
              AND (locked_at IS NULL OR locked_at < :lockExpiredBefore)
            RETURNING id
            """;

        Map<String, Object> params = new HashMap<>();
        params.put("now", now);
        params.put("lockedBy", lockedBy);
        params.put("lockExpiredBefore", lockExpiredBefore);
        params.put("ids", locked.toArray(UUID[]::new));

        return jdbc.query(updateSql, params, (rs, rowNum) -> UUID.fromString(rs.getString("id")));
    }

    private List<UUID> selectCandidateIds(OffsetDateTime now, OffsetDateTime lockExpiredBefore, int limit) {

        String sql = """
            SELECT id
            FROM outbox_messages
            WHERE processed_at IS NULL
              AND (next_attempt_at IS NULL OR next_attempt_at <= :now)
              AND (locked_at IS NULL OR locked_at < :lockExpiredBefore)
            ORDER BY occurred_at
            LIMIT :limit
            """;

        Map<String, Object> params = new HashMap<>();
        params.put("now", now);
        params.put("lockExpiredBefore", lockExpiredBefore);
        params.put("limit", limit);

        return jdbc.query(sql, params, (rs, rowNum) -> UUID.fromString(rs.getString("id")));
    }

    private boolean tryAdvisoryLock(UUID id) {

        /*
            pg_try_advisory_lock -> zwraca true → lock został założony, false -> ktoś inny już trzyma ten lock
            - advisory lock jest powiązany z połączeniem (session)
            - NIE z transakcją (chyba że używasz _xact_ wariantu)
            - jeśli connection żyje → lock żyje
            - jeśli connection się zamknie (commit, rollback, crash JVM) → lock znika
             Poniższa metoda determinuje stabilny klucz BIGINT z UUID w Postgres:
             bierze pierwsze 16 znaków szesnastkowych z md5(uuid)
             i interpretuje ją jako 64-bitową liczbę całkowitą ze znakiem.
         */
        String sql = """
            SELECT pg_try_advisory_lock(
                (('x' || substr(md5(:uuidText), 1, 16))::bit(64))::bigint
            ) AS locked
            """;

        Map<String, Object> params = Map.of("uuidText", id.toString());

        Boolean locked = jdbc.queryForObject(sql, params, Boolean.class);
        return Boolean.TRUE.equals(locked);
    }
}
