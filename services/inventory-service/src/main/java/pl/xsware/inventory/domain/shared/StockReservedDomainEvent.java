package pl.xsware.inventory.domain.shared;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record StockReservedDomainEvent(
    UUID eventId,
    Instant occurredAt,
    String eventType,
    int version,
    UUID orderId,
    UUID reservationId,
    List<Line> lines
) implements DomainEvent {

    public static final String TYPE = "StockReserved";
    public static final int VERSION = 1;

    public static StockReservedDomainEvent of(
        UUID orderId,
        UUID reservationId,
        List<Line> lines
    ) {
        return new StockReservedDomainEvent(
            UUID.randomUUID(),
            Instant.now(),
            TYPE,
            VERSION,
            orderId,
            reservationId,
            lines
        );
    }

    @Override
    public String eventType() {
        return TYPE;
    }

    @Override
    public int version() {
        return VERSION;
    }

    public record Line(
        String sku,
        String warehouse,
        int quantity
    ) {}
}
