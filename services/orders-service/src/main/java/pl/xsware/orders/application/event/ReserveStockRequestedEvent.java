package pl.xsware.orders.application.event;

import pl.xsware.orders.domain.shared.OutboxEvent;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ReserveStockRequestedEvent(
    UUID eventId,
    String eventType,
    int version,
    Instant occurredAt,
    Data data
) implements OutboxEvent {
    public static final String TYPE = "ReserveStockRequested";
    public static final int VERSION = 1;

    public static ReserveStockRequestedEvent of(
        UUID orderId,
        UUID correlationId,
        Instant expiresAt,
        List<Item> items
    ) {
        return new ReserveStockRequestedEvent(
            UUID.randomUUID(),
            TYPE,
            VERSION,
            Instant.now(),
            new Data(orderId, correlationId, expiresAt, items)
        );
    }

    @Override
    public String aggregateType() {
        return "ORDER";
    }

    @Override
    public String aggregateId() {
        return data.orderId().toString();
    }

    public record Data(
        UUID orderId,
        UUID correlationId,
        Instant expiresAt,
        List<Item> items
    ) {}

    public record Item(
        String sku,
        int quantity
    ) {}
}
