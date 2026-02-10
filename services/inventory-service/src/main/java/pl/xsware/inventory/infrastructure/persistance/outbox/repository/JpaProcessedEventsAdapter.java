package pl.xsware.inventory.infrastructure.persistance.outbox.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import pl.xsware.inventory.application.event.ProcessedEventsPort;
import pl.xsware.inventory.infrastructure.persistance.outbox.entity.ProcessedEventEntity;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Repository
@RequiredArgsConstructor
public class JpaProcessedEventsAdapter implements ProcessedEventsPort {

    private final ProcessedEventJpaRepository processedRepo;

    @Override
    public boolean exists(UUID eventId) {
        return processedRepo.existsByEventId(eventId);
    }

    @Override
    public void markProcessed(UUID eventId, String eventType, UUID aggregateId, Instant occurredAt) {

        try {
            ProcessedEventEntity e = new ProcessedEventEntity();
            e.setEventId(eventId);
            e.setEventType(eventType);
            e.setAggregateId(aggregateId);
            e.setOccurredAt(occurredAt);
            processedRepo.save(e);
        } catch (Exception ex) {
            log.warn("ProcessedEvent already exists (race/duplicate). eventId={}, eventType={}, aggregateId={}",
                eventId, eventType, aggregateId);
        }
    }
}
