package pl.xsware.orders.domain.order;

public enum OrderStatus {

    CREATED,
    PAYMENT_PENDING,

    PAID,
    PAYMENT_FAILED,
    CANCELLED
}
