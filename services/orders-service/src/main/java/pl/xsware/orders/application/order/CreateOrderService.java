package pl.xsware.orders.application.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.xsware.orders.application.event.PaymentRequestedEvent;
import pl.xsware.orders.application.outbox.OutboxWriter;
import pl.xsware.orders.domain.order.Currency;
import pl.xsware.orders.domain.order.Order;
import pl.xsware.orders.domain.order.OrderRepository;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreateOrderService implements CreateOrderUseCase{

    private final OrderRepository orderRepository;
    private final OutboxWriter outboxWriter;

    @Override
    @Transactional
    public void create(CreateOrderCommand command) {

        // TODO: Add counting total amount from product list
        Order order = Order.create(command.customerId(), command.totalAmount(), Currency.PLN);
        orderRepository.save(order);

        outboxWriter.writeAll(order.pullDomainEvents());

        PaymentRequestedEvent event =
            PaymentRequestedEvent.builder()
                .eventId(UUID.randomUUID())
                .eventType(PaymentRequestedEvent.TYPE)
                .version(PaymentRequestedEvent.VERSION)
                .occurredAt(Instant.now())
                .data(PaymentRequestedEvent.Data.builder()
                    .orderId(order.getId().value())
                    .amount(order.getTotalAmount())
                    .currency(order.getCurrency())
                    .build()
                )
                .build();

        log.debug("UC_CREATE_ORDER domainEvent=OrderCreatedEvent orderId={} customerId={}",
            order.getId().value(), order.getCustomerId());
    }
}
