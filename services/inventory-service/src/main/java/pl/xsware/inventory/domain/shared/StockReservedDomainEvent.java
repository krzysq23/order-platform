package pl.xsware.inventory.domain.shared;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record StockReservedDomainEvent(
    UUID eventId,
    Instant occurredAt,
    UUID orderId,
    UUID reservationId,
    List<Line> lines
) implements DomainEvent {

    public static final String TYPE = "StockReserved";
    public static final int VERSION = 1;

    @Override public String eventType() { return TYPE; }
    @Override public int version() { return VERSION; }

    public record Line(String sku, String warehouse, int quantity) {}
}
