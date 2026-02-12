package pl.xsware.orders.application.event;

import java.time.Instant;
import java.util.UUID;

public record StockReservationFailedEvent(
    UUID eventId,
    String eventType,
    int version,
    Instant occurredAt,
    Data data
) {
    public static final String TYPE = "StockReservationFailed";
    public static final int VERSION = 1;

    public record Data(
        UUID orderId,
        UUID reservationId,
        String reason
    ) {}
}
