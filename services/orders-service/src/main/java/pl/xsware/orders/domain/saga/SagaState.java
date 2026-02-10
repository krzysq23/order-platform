package pl.xsware.orders.domain.saga;

public enum SagaState {
    INVENTORY_REQUESTED,
    PAYMENT_REQUESTED,
    INVENTORY_RESERVED,
    PAID,
    FAILED,
    CANCELLED
}
