package pl.xsware.inventory.domain.shared;

import java.time.Instant;
import java.util.UUID;

public interface DomainEvent {

    UUID eventId();
    String eventType();
    int version();
    Instant occurredAt();
}
