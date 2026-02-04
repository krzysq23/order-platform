package pl.xsware.orders.domain.order;

import pl.xsware.orders.domain.shared.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record OrderCreatedEvent(
    UUID orderId,
    String customerId,
    Instant occurredAt
) implements DomainEvent {

    public static OrderCreatedEvent now(Order order) {
        return new OrderCreatedEvent(
            order.getId().value(),
            order.getCustomerId(),
            Instant.now()
        );
    }
}
