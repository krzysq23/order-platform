package pl.xsware.orders.application.order;

import java.math.BigDecimal;

public record CreateOrderCommand(String customerId, BigDecimal totalAmount) {
}
