package pl.xsware.inventory.application.outbox;

import pl.xsware.inventory.domain.shared.DomainEvent;

import java.util.List;

public interface OutboxPort {

    void enqueueAll(List<DomainEvent> events, String aggregateType, String aggregateId);
}
