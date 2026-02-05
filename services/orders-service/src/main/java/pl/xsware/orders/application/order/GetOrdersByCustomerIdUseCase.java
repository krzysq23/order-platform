package pl.xsware.orders.application.order;

import pl.xsware.orders.domain.order.Order;

import java.util.List;

public interface GetOrdersByCustomerIdUseCase {

    List<Order> getByCustomerId(String customerId);
}
