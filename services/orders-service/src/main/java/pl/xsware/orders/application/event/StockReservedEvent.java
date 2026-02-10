package pl.xsware.orders.application.event;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record StockReservedEvent(
    UUID eventId,
    String eventType,
    int version,
    Instant occurredAt,
    Data data
) {
    public static final String TYPE = "StockReserved";
    public static final int VERSION = 1;

    public record Data(UUID orderId, UUID reservationId, List<Line> lines) {
        public record Line(String sku, String warehouse, int quantity) {}
    }
}
