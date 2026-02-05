package pl.xsware.orders.outbox;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import pl.xsware.orders.application.outbox.OutboxDispatcher;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(FailingPublisherConfig.class)
class OutboxRetryIT extends OutboxClaimITBase {

    @Autowired
    OutboxDispatcher dispatcher;

    @Autowired
    NamedParameterJdbcTemplate jdbc;

    @Test
    void shouldScheduleRetryWhenPublishFails() {
        UUID id = insertSingleOutboxMessage();

        boolean result = dispatcher.processOne(id);

        assertThat(result).isFalse();

        Map<String, Object> row = jdbc.queryForMap(
            "SELECT processed_at, attempts, next_attempt_at, locked_at, locked_by, last_error FROM outbox_messages WHERE id = :id",
            Map.of("id", id)
        );

        assertThat(row.get("processed_at")).isNull();
        assertThat(((Number) row.get("attempts")).intValue()).isEqualTo(1);
        assertThat(row.get("next_attempt_at")).isNotNull();
        assertThat(row.get("locked_at")).isNull();
        assertThat(row.get("locked_by")).isNull();
        assertThat((String) row.get("last_error")).contains("Simulated publish failure");
    }

    private UUID insertSingleOutboxMessage() {
        UUID id = UUID.randomUUID();

        String sql = """
            INSERT INTO outbox_messages (
                id,
                aggregate_type,
                aggregate_id,
                event_type,
                payload,
                occurred_at,
                created_at,
                processed_at,
                attempts,
                next_attempt_at,
                locked_at,
                locked_by,
                last_error
            )
            VALUES (
                :id,
                'Order',
                'ORDER-1',
                'OrderCreatedEvent',
                '{"id":"test"}',
                :now,
                :now,
                NULL,
                0,
                NULL,
                NULL,
                NULL,
                NULL
            )
            """;

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        jdbc.update(sql, Map.of(
            "id", id,
            "now", now
        ));

        return id;
    }
}
