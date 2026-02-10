package pl.xsware.orders.application.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.xsware.orders.application.event.PaymentRequestedEvent;
import pl.xsware.orders.application.event.PaymentRequestedEventFactory;
import pl.xsware.orders.application.event.ReserveStockRequestedEvent;
import pl.xsware.orders.application.outbox.OutboxWriter;
import pl.xsware.orders.application.saga.OrderPaymentSagaService;
import pl.xsware.orders.domain.order.Currency;
import pl.xsware.orders.domain.order.Order;
import pl.xsware.orders.domain.order.OrderRepository;
import pl.xsware.orders.domain.saga.SagaState;
import pl.xsware.orders.infrastructure.persistence.saga.SagaInstanceEntity;
import pl.xsware.orders.infrastructure.persistence.saga.SagaInstanceRepository;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
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

        Order order = Order.create(command.customerId(), command.totalAmount(), Currency.PLN);

        orderRepository.save(order);

        SagaInstanceEntity saga = SagaInstanceEntity.start(
            UUID.randomUUID(),
            OrderPaymentSagaService.SAGA_TYPE,
            order.getId().value().toString(),
            objectMapper
        );
        saga.transitionTo(SagaState.INVENTORY_REQUESTED);

        sagaRepo.save(saga);

        ReserveStockRequestedEvent reserveEvent = ReserveStockRequestedEvent.of(
            order.getId().value(),
            saga.getSagaId(),
            Instant.now().plus(15, ChronoUnit.MINUTES),
            command.items().stream()
                .map(i -> new ReserveStockRequestedEvent.Item(i.sku(), i.quantity()))
                .toList()
        );
        outboxWriter.write(reserveEvent);

        log.debug("UC_CREATE_ORDER inventoryRequested orderId={} customerId={}",
            order.getId().value(), order.getCustomerId());
    }
}
