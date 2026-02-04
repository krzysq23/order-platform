package pl.xsware.orders.application.order;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.xsware.orders.domain.order.Order;
import pl.xsware.orders.domain.order.OrderRepository;

@Service
@RequiredArgsConstructor
public class CreateOrderService implements CreateOrderUseCase{

    private final OrderRepository orderRepository;

    @Override
    @Transactional
    public void create(CreateOrderCommand command) {

        Order order = Order.create(command.customerId());
        orderRepository.save(order);
    }
}
