package pl.xsware.inventory.application.event;

import java.time.Instant;
import java.util.UUID;

public interface ProcessedEventsPort {

    boolean exists(UUID eventId);

    void markProcessed(UUID eventId, String eventType, UUID aggregateId, Instant occurredAt);
}
