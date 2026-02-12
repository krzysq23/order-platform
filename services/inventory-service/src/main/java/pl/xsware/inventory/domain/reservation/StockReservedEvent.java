package pl.xsware.inventory.domain.reservation;

import java.util.List;
import java.util.UUID;

public record StockReservedEvent(
    UUID orderId,
    UUID reservationId,
    List<StockReservation.Line> lines)
{}
