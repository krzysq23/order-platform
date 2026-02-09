package pl.xsware.orders.domain.saga;

public enum SagaState {
    PAYMENT_REQUESTED,
    PAID,
    FAILED,
    CANCELLED
}
