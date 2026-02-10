package pl.xsware.orders.infrastructure.persistence.outbox;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.xsware.orders.application.outbox.OutboxWriter;
import pl.xsware.orders.domain.shared.OutboxEvent;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.exc.JsonNodeException;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@RequiredArgsConstructor
@Component
public class OutboxWriterImpl implements OutboxWriter {


    private final OutboxJpaRepository outboxJpaRepository;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    @Override
    public void write(OutboxEvent event) {

        Instant now = Instant.now(clock);

        OutboxMessageEntity entity = OutboxMessageEntity.builder()
            .id(UUID.randomUUID())
            .eventId(event.eventId())
            .aggregateType(event.aggregateType())
            .aggregateId(event.aggregateId())
            .eventType(event.eventType())
            .payload(toJson(event))
            .occurredAt(event.occurredAt())
            .createdAt(now)
            .attempts(0)
            .build();

        outboxJpaRepository.save(entity);
    }

    private String toJson(OutboxEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonNodeException e) {
            throw new IllegalStateException(
                "Failed to serialize outbox event to JSON: " + event.eventType(),
                e
            );
        }
    }

}
