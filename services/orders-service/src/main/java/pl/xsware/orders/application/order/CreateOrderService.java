package pl.xsware.orders.application.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.xsware.orders.application.event.PaymentRequestedEvent;
import pl.xsware.orders.application.event.PaymentRequestedEventFactory;
import pl.xsware.orders.application.outbox.OutboxWriter;
import pl.xsware.orders.domain.order.Currency;
import pl.xsware.orders.domain.order.Order;
import pl.xsware.orders.domain.order.OrderRepository;
import pl.xsware.orders.infrastructure.messaging.kafka.PaymentRequestedKafkaPublisher;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreateOrderService implements CreateOrderUseCase{

    private final OrderRepository orderRepository;
    private final OutboxWriter outboxWriter;
    private final PaymentRequestedKafkaPublisher paymentRequestedKafkaPublisher;

    @Override
    @Transactional
    public void create(CreateOrderCommand command) {

        // TODO: Add counting total amount from product list
        Order order = Order.create(command.customerId(), command.totalAmount(), Currency.PLN);
        orderRepository.save(order);

        outboxWriter.writeAll(order.pullDomainEvents());

        var event = PaymentRequestedEventFactory.create(order.getId().value(), order.getTotalAmount(), order.getCurrency());
        paymentRequestedKafkaPublisher.publish(event);

        log.debug("UC_CREATE_ORDER domainEvent=OrderCreatedEvent orderId={} customerId={}",
            order.getId().value(), order.getCustomerId());
    }
}
