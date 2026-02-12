package pl.xsware.inventory.domain.shared;

import java.time.Instant;
import java.util.UUID;

public record StockReservationFailedDomainEvent(
    UUID eventId,
    Instant occurredAt,
    UUID orderId,
    UUID reservationId,
    String reason
) implements DomainEvent {

    public static final String TYPE = "StockReservationFailed";
    public static final int VERSION = 1;

    @Override public String eventType() { return TYPE; }
    @Override public int version() { return VERSION; }
}
