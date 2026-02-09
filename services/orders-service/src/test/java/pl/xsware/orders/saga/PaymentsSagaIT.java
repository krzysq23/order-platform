package pl.xsware.orders.saga;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import pl.xsware.orders.application.event.PaymentSucceededEvent;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
    properties = "orders.outbox.dispatcher.enabled=false"
)
@EmbeddedKafka(
    partitions = 1,
    topics = {
        "payments.payment-succeeded.v1",
        "payments.payment-failed.v1",
        "payments.payment-cancelled.v1"
    }
)
@Import(JacksonTestConfig.class)
class PaymentsSagaIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
        .withDatabaseName("orders")
        .withUsername("orders")
        .withPassword("orders");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {

        r.add("spring.datasource.url", postgres::getJdbcUrl);
        r.add("spring.datasource.username", postgres::getUsername);
        r.add("spring.datasource.password", postgres::getPassword);

        r.add("spring.kafka.bootstrap-servers",
            () -> System.getProperty("spring.embedded.kafka.brokers"));

        r.add("spring.kafka.consumer.properties.spring.json.trusted.packages",
            () -> "pl.xsware.orders.application");
        r.add("spring.kafka.consumer.properties.spring.json.use.type.headers",
            () -> "false");
    }

    @Autowired
    JdbcTemplate jdbc;

    @Autowired
    KafkaTemplate<String, PaymentSucceededEvent> kafkaTemplate;

    @Test
    void paymentSucceeded_should_mark_order_paid_update_saga_and_write_outbox_once() {
        UUID orderId = UUID.randomUUID();
        UUID sagaId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();

        seedOrder(orderId);
        seedSaga(sagaId, orderId);

        PaymentSucceededEvent evt = PaymentSucceededEvent.builder()
            .eventId(eventId)
            .eventType(PaymentSucceededEvent.TYPE)
            .version(PaymentSucceededEvent.VERSION)
            .occurredAt(Instant.now())
            .data(PaymentSucceededEvent.Data.builder()
                .orderId(orderId)
                .paymentId(paymentId)
                .provider("mock")
                .externalId("ext-123")
                .build())
            .build();

        kafkaTemplate.send("payments.payment-succeeded.v1", orderId.toString(), evt);

        // 1) order status -> PAID
        Awaitility.await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            String status = jdbc.queryForObject(
                "select status from orders where id = ?",
                String.class,
                orderId
            );
            assertThat(status).isEqualTo("PAID");
        });

        // 2) saga state -> PAID
        String sagaState = jdbc.queryForObject(
            "select state from saga_instances where saga_id = ?",
            String.class,
            sagaId
        );
        assertThat(sagaState).isEqualTo("PAID");

        // 3) outbox contains OrderPaid
        Integer outboxCount = jdbc.queryForObject(
            "select count(*) from outbox_messages where aggregate_id = ? and event_type = ?",
            Integer.class,
            orderId.toString(),
            "OrderPaid"
        );
        assertThat(outboxCount).isEqualTo(1);

        // 4) idempotencja
        kafkaTemplate.send("payments.payment-succeeded.v1", orderId.toString(), evt);

        Awaitility.await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            Integer outboxCount2 = jdbc.queryForObject(
                "select count(*) from outbox_messages where aggregate_id = ? and event_type = ?",
                Integer.class,
                orderId.toString(),
                "OrderPaid"
            );
            assertThat(outboxCount2).isEqualTo(1);
        });
    }

    private void seedOrder(UUID orderId) {
        jdbc.update("""
                insert into orders (id, customer_id, status, created_at, total_amount, currency)
                values (?, ?, ?, ?, ?, ?)
            """,
            orderId,
            "cust-1",
            "PAYMENT_PENDING",
            OffsetDateTime.now(ZoneOffset.UTC),
            new BigDecimal("10.00"),
            "PLN"
        );
    }

    private void seedSaga(UUID sagaId, UUID orderId) {
        jdbc.update("""
                insert into saga_instances (saga_id, saga_type, aggregate_id, state, data, created_at, updated_at)
                values (?, ?, ?, ?, '{}'::jsonb, now(), now())
            """,
            sagaId,
            "ORDER_PAYMENT_SAGA",
            orderId.toString(),
            "PAYMENT_REQUESTED"
        );
    }
}
