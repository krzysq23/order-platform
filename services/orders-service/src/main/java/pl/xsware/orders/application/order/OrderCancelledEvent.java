package pl.xsware.orders.application.order;

import pl.xsware.orders.domain.order.Order;
import pl.xsware.orders.domain.shared.OutboxEvent;

import java.time.Instant;
import java.util.UUID;

public record OrderCancelledEvent(
    UUID eventId,
    String eventType,
    int version,
    Instant occurredAt,
    String orderId,
    String reason
) implements OutboxEvent {

    public static final String TYPE = "OrderCancelled";
    public static final int VERSION = 1;

    public static OrderCancelledEvent now(Order order, String reason) {
        return new OrderCancelledEvent(
            UUID.randomUUID(),
            TYPE,
            VERSION,
            Instant.now(),
            order.getId().toString(),
            reason
        );
    }

    @Override
    public String aggregateType() {
        return "Order";
    }

    @Override
    public String aggregateId() {
        return orderId;
    }
}
