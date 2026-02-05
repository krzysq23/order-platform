package pl.xsware.orders.outbox;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Testcontainers
public abstract class OutboxClaimITBase {

    static final PostgreSQLContainer<?> POSTGRES =
        new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("orders")
            .withUsername("orders")
            .withPassword("orders");

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        r.add("spring.datasource.username", POSTGRES::getUsername);
        r.add("spring.datasource.password", POSTGRES::getPassword);

        r.add("spring.flyway.enabled", () -> "true");
        r.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    }

    @Autowired
    NamedParameterJdbcTemplate jdbc;

    @BeforeEach
    void clean() {
        jdbc.getJdbcTemplate().execute("DELETE FROM outbox_messages");
    }

    protected void insertOutboxMessages(int count) {

        String sql = """
            INSERT INTO outbox_messages (id, aggregate_type, aggregate_id, event_type, payload, occurred_at, created_at,
                processed_at, attempts, next_attempt_at, locked_at, locked_by, last_error)
            VALUES (:id, :aggregateType, :aggregateId, :eventType, :payload, :occurredAt, :createdAt, NULL,0,NULL,NULL,NULL,NULL)
            """;

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        for (int i = 0; i < count; i++) {
            UUID id = UUID.randomUUID();
            Map<String, Object> p = new HashMap<>();
            p.put("id", id);
            p.put("aggregateType", "Order");
            p.put("aggregateId", "CUST-" + (i % 10));
            p.put("eventType", "OrderCreatedEvent");
            p.put("payload", "{\"id\":\"" + id + "\"}");
            p.put("occurredAt", now.minusSeconds(count - i));
            p.put("createdAt", now.minusSeconds(count - i));
            jdbc.update(sql, p);
        }
    }
}
