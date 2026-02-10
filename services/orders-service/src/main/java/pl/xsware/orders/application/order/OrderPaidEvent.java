package pl.xsware.orders.application.order;

import pl.xsware.orders.domain.shared.OutboxEvent;

import java.time.Instant;
import java.util.UUID;

public record OrderPaidEvent(
    UUID eventId,
    String eventType,
    int version,
    Instant occurredAt,
    String orderId
) implements OutboxEvent {

    public static final String TYPE = "OrderPaid";
    public static final int VERSION = 1;

    public static OrderPaidEvent of(UUID orderId) {
        return new OrderPaidEvent(
            UUID.randomUUID(),
            TYPE,
            VERSION,
            Instant.now(),
            orderId.toString()
        );
    }

    @Override
    public String aggregateType() {
        return "ORDER";
    }

    @Override
    public String aggregateId() {
        return orderId;
    }
}
