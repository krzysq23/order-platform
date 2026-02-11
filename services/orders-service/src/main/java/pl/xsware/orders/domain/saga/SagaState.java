package pl.xsware.orders.domain.saga;

public enum SagaState {
    INVENTORY_REQUESTED,
    INVENTORY_RESERVED,
    PAYMENT_REQUESTED,
    PAID,
    PAYMENT_FAILED,
    CANCELLED
}
