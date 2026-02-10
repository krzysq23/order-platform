package pl.xsware.orders.application.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.xsware.orders.application.event.PaymentRequestedEvent;
import pl.xsware.orders.application.event.PaymentRequestedEventFactory;
import pl.xsware.orders.application.outbox.OutboxWriter;
import pl.xsware.orders.application.saga.OrderPaymentSagaService;
import pl.xsware.orders.domain.order.Currency;
import pl.xsware.orders.domain.order.Order;
import pl.xsware.orders.domain.order.OrderRepository;
import pl.xsware.orders.infrastructure.persistence.saga.SagaInstanceEntity;
import pl.xsware.orders.infrastructure.persistence.saga.SagaInstanceRepository;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreateOrderService implements CreateOrderUseCase{

    private final OrderRepository orderRepository;
    private final OutboxWriter outboxWriter;
    private final SagaInstanceRepository sagaRepo;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void create(CreateOrderCommand command) {

        // TODO: Add counting total amount from product list
        Order order = Order.create(command.customerId(), command.totalAmount(), Currency.PLN);
        orderRepository.save(order);

        PaymentRequestedEvent event =
            PaymentRequestedEventFactory.create(
                order.getId().value(),
                order.getTotalAmount(),
                order.getCurrency()
            );

        outboxWriter.write(event);


        SagaInstanceEntity saga = SagaInstanceEntity.start(
            UUID.randomUUID(),
            OrderPaymentSagaService.SAGA_TYPE,
            order.getId().value().toString(),
            objectMapper
        );
        sagaRepo.save(saga);

        log.debug("UC_CREATE_ORDER domainEvent=OrderCreatedEvent orderId={} customerId={}",
            order.getId().value(), order.getCustomerId());
    }
}
