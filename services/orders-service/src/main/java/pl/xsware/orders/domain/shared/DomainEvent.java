package pl.xsware.orders.domain.shared;

import java.time.Instant;

public interface DomainEvent {

    Instant occurredAt();
}
