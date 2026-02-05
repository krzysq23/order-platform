package pl.xsware.orders.infrastructure.persistence.outbox;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class OutboxLagRepository {

    private final NamedParameterJdbcTemplate jdbc;

    /**
     *  Zwraca opóźnienie w sekundach dla najstarszej nieprzetworzonej wiadomości wychodzącej.
     *  0, jeśli nie ma oczekujących wiadomości.
     */
    public long computeLagSeconds(OffsetDateTime now) {
        String sql = """
            SELECT COALESCE(
              EXTRACT(EPOCH FROM (:now - MIN(occurred_at))),
              0
            )::bigint AS lag_seconds
            FROM outbox_messages
            WHERE processed_at IS NULL
            """;

        Long lag = jdbc.queryForObject(sql, Map.of("now", now), Long.class);
        return lag == null ? 0 : lag;
    }
}
