package pl.xsware.orders.application.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.xsware.orders.application.outbox.OutboxWriter;
import pl.xsware.orders.domain.order.Order;
import pl.xsware.orders.domain.order.OrderRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreateOrderService implements CreateOrderUseCase{

    private final OrderRepository orderRepository;
    private final OutboxWriter outboxWriter;

    @Override
    @Transactional
    public void create(CreateOrderCommand command) {

        Order order = Order.create(command.customerId());
        orderRepository.save(order);

        outboxWriter.writeAll(order.pullDomainEvents());

        log.debug("UC_CREATE_ORDER domainEvent=OrderCreatedEvent orderId={} customerId={}",
            order.getId().value(), order.getCustomerId());
    }
}
