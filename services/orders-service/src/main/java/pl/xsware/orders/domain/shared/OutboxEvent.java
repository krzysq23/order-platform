package pl.xsware.orders.domain.shared;

import java.util.UUID;

public interface OutboxEvent extends DomainEvent {

    UUID eventId();

    int version();

    String aggregateType();

    String aggregateId();

    String eventType();
}
