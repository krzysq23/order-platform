package pl.xsware.inventory.application.reservation;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ReserveStockRequest(
    UUID eventId,
    String eventType,
    Instant occurredAt,
    UUID orderId,
    UUID correlationId,
    Instant expiresAt,
    List<Item> items
) {
    public record Item(String sku, int quantity) {}
}
