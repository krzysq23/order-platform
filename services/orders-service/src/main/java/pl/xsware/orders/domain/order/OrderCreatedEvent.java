package pl.xsware.orders.domain.order;

import pl.xsware.orders.domain.shared.OutboxEvent;

import java.time.Instant;
import java.util.UUID;

public record OrderCreatedEvent(
    UUID orderId,
    String customerId,
    Instant occurredAt
) implements OutboxEvent {

    public static OrderCreatedEvent now(Order order) {
        return new OrderCreatedEvent(
            order.getId().value(),
            order.getCustomerId(),
            Instant.now()
        );
    }

    @Override
    public String aggregateType() {
        return "Order";
    }

    @Override
    public String aggregateId() {
        return orderId.toString();
    }

    @Override
    public String eventType() {
        return "OrderCreated";
    }
}
