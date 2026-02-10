package pl.xsware.inventory.infrastructure.persistance.outbox.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pl.xsware.inventory.application.outbox.OutboxPort;
import pl.xsware.inventory.domain.shared.DomainEvent;
import pl.xsware.inventory.infrastructure.persistance.outbox.entity.OutboxMessageEntity;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class JpaOutboxAdapter implements OutboxPort {

    private final OutboxMessageJpaRepository outboxRepo;
    private final ObjectMapper objectMapper;

    @Override
    public void enqueueAll(List<DomainEvent> events, String aggregateType, String aggregateId) {
        if (events == null || events.isEmpty()) return;

        var messages = events.stream()
            .map(ev -> toOutboxMessage(ev, aggregateType, aggregateId))
            .toList();

        outboxRepo.saveAll(messages);
        log.debug("Outbox enqueued. count={}, aggregateType={}, aggregateId={}",
            messages.size(), aggregateType, aggregateId);
    }

    private OutboxMessageEntity toOutboxMessage(DomainEvent ev, String aggregateType, String aggregateId) {

        OutboxMessageEntity m = new OutboxMessageEntity();
        m.setEventId(ev.eventId());
        m.setEventType(ev.eventType());
        m.setVersion(ev.version());
        m.setOccurredAt(ev.occurredAt());

        m.setAggregateType(aggregateType);
        m.setAggregateId(java.util.UUID.fromString(aggregateId));

        m.setKey(aggregateId);

        @SuppressWarnings("unchecked")
        Map<String, Object> payload = objectMapper.convertValue(ev, Map.class);
        m.setPayload(payload);

        return m;
    }
}
