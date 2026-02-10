package pl.xsware.inventory.application.event;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ReserveStockRequestedEvent(
    UUID eventId,
    String eventType,
    int version,
    Instant occurredAt,
    Data data
) {
    public static final String TYPE = "ReserveStockRequested";
    public static final int VERSION = 1;

    public record Data(
        UUID orderId,
        UUID correlationId,
        Instant expiresAt,
        List<Item> items
    ) {
        public record Item(String sku, int quantity) {}
    }
}
