package pl.xsware.orders.application.order;

import pl.xsware.orders.domain.order.Order;
import pl.xsware.orders.domain.shared.OutboxEvent;

import java.time.Instant;
import java.util.UUID;

public record OrderConfirmedEvent(
    UUID eventId,
    String eventType,
    int version,
    Instant occurredAt,
    String orderId
) implements OutboxEvent {

    public static final String TYPE = "OrderConfirmed";
    public static final int VERSION = 1;

    public static OrderConfirmedEvent now(Order order) {
        return new OrderConfirmedEvent(
            UUID.randomUUID(),
            TYPE,
            VERSION,
            Instant.now(),
            order.getId().toString()
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
