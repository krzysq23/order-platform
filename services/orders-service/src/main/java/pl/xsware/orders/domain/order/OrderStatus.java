package pl.xsware.orders.domain.order;

public enum OrderStatus {

    CREATED,
    INVENTORY_PENDING,
    PAYMENT_PENDING,
    PAID,
    PAYMENT_FAILED,
    CANCELLED
}
