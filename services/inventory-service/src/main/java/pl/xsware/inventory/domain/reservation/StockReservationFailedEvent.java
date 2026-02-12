package pl.xsware.inventory.domain.reservation;

import java.util.UUID;

public record StockReservationFailedEvent(
    UUID orderId,
    String reason)
{}
