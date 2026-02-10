package pl.xsware.inventory.domain.reservation;

public enum StockReservationStatus {
    REQUESTED,
    RESERVED,
    FAILED,
    CANCELLED,
    RELEASED,
    EXPIRED
}
