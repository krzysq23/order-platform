package pl.xsware.orders.domain.order;

import java.util.Optional;

public interface OrderRepository {

    void save(Order order);
    Optional<Order> findById(OrderId orderId);
}
