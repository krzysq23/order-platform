package pl.xsware.orders.infrastructure.persistence.outbox;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.xsware.orders.application.outbox.OutboxWriter;
import pl.xsware.orders.domain.shared.OutboxEvent;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.UUID;

@RequiredArgsConstructor
@Component
public class OutboxWriterImpl implements OutboxWriter {


    private final OutboxJpaRepository outboxJpaRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void write(OutboxEvent event) {
        OutboxMessageEntity entity = new OutboxMessageEntity(
            UUID.randomUUID(),
            event.aggregateType(),
            event.aggregateId(),
            event.eventType(),
            toJson(event),
            event.occurredAt(),
            Instant.now(),
            null
        );

        outboxJpaRepository.save(entity);
    }

    private String toJson(OutboxEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JacksonException e) {
            throw new IllegalStateException(
                "Failed to serialize outbox event to JSON: " + event.eventType(),
                e
            );
        }
    }

}
