package pl.xsware.inventory.infrastructure.web.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record StockReservationDto(
    UUID reservationId,
    UUID orderId,
    String warehouse,
    String status,
    Instant createdAt,
    Instant expiresAt,
    List<Line> lines
) {
    public record Line(
        String sku,
        int quantity
    ) {}
}
