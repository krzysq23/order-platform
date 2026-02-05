package pl.xsware.orders.domain.shared;


public interface OutboxEvent extends DomainEvent {

    String aggregateType();

    String aggregateId();

    String eventType();
}
