package pl.xsware.orders.application.saga;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.xsware.orders.application.event.*;
import pl.xsware.orders.application.order.OrderPaymentUpdater;
import pl.xsware.orders.application.outbox.OutboxWriter;
import pl.xsware.orders.domain.order.Order;
import pl.xsware.orders.domain.order.OrderId;
import pl.xsware.orders.domain.order.OrderRepository;
import pl.xsware.orders.domain.saga.SagaState;
import pl.xsware.orders.infrastructure.persistence.saga.ProcessedEventEntity;
import pl.xsware.orders.infrastructure.persistence.saga.ProcessedEventRepository;
import pl.xsware.orders.infrastructure.persistence.saga.SagaInstanceEntity;
import pl.xsware.orders.infrastructure.persistence.saga.SagaInstanceRepository;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderPaymentSagaService {

    public static final String SAGA_TYPE = "ORDER_PAYMENT_SAGA";

    private final SagaInstanceRepository sagaRepo;
    private final ProcessedEventRepository processedRepo;

    private final OrderInventoryUpdater orderInventoryUpdater;
    private final OutboxWriter outboxWriter;

    private final OrderRepository orderRepository;

    private final OrderPaymentUpdater orderPaymentUpdater;
    private final ObjectMapper objectMapper;

    @Transactional
    public void handle(PaymentSucceededEvent event) {
        if (alreadyProcessed(event.eventId())) return;

        UUID orderId = event.data().orderId();
        SagaInstanceEntity saga = loadSagaOrThrow(orderId);

        if (isFinal(saga.getState())) return;

        requireState(saga, SagaState.PAYMENT_REQUESTED);

        orderPaymentUpdater.markPaid(
            orderId,
            event.data().paymentId(),
            event.data().provider(),
            event.data().externalId()
        );

        saga.transitionTo(SagaState.PAID);
        saga.putUuid("paymentId", event.data().paymentId(), objectMapper);
        saga.putString("provider", event.data().provider(), objectMapper);
        saga.putString("externalId", event.data().externalId(), objectMapper);

        sagaRepo.save(saga);
    }

    @Transactional
    public void handle(PaymentFailedEvent event) {
        if (alreadyProcessed(event.eventId())) return;

        UUID orderId = event.data().orderId();
        SagaInstanceEntity saga = loadSagaOrThrow(orderId);

        if (isFinal(saga.getState())) return;

        requireState(saga, SagaState.PAYMENT_REQUESTED);

        orderPaymentUpdater.markPaymentFailed(orderId, event.data().reason());

        saga.transitionTo(SagaState.PAYMENT_FAILED);
        saga.putUuid("paymentId", event.data().paymentId(), objectMapper);
        saga.putString("reason", event.data().reason(), objectMapper);

        sagaRepo.save(saga);
    }

    @Transactional
    public void handle(PaymentCancelledEvent event) {
        if (alreadyProcessed(event.eventId())) return;

        UUID orderId = event.data().orderId();
        SagaInstanceEntity saga = loadSagaOrThrow(orderId);

        if (isFinal(saga.getState())) return;

        requireState(saga, SagaState.PAYMENT_REQUESTED);

        orderPaymentUpdater.markPaymentCancelled(orderId, event.data().reason());

        saga.transitionTo(SagaState.CANCELLED);
        saga.putUuid("paymentId", event.data().paymentId(), objectMapper);
        saga.putString("reason", event.data().reason(), objectMapper);

        sagaRepo.save(saga);
    }

    @Transactional
    public void handle(StockReservedEvent event) {
        if (alreadyProcessed(event.eventId())) return;

        UUID orderId = event.orderId();
        SagaInstanceEntity saga = loadSagaOrThrow(orderId);

        if (isFinal(saga.getState())) return;

        requireState(saga, SagaState.INVENTORY_REQUESTED);

        orderInventoryUpdater.markInventoryReserved(orderId, event);

        saga.putUuid("inventoryReservationId", event.reservationId(), objectMapper);
        saga.transitionTo(SagaState.INVENTORY_RESERVED);

        OrderId oid = OrderId.of(orderId);
        Order order = orderRepository.findById(oid)
            .orElseThrow(() ->
                new IllegalStateException("Order not found: " + oid));

        order.startPayment();
        orderRepository.save(order);

        PaymentRequestedEvent paymentRequested = PaymentRequestedEventFactory.create(
            order.getId().value(),
            order.getTotalAmount(),
            order.getCurrency()
        );

        outboxWriter.write(paymentRequested);

        saga.transitionTo(SagaState.PAYMENT_REQUESTED);

        sagaRepo.save(saga);
        markProcessed(event.eventId());
    }

    @Transactional
    public void handle(StockReservationFailedEvent event) {
        if (alreadyProcessed(event.eventId())) return;

        UUID orderId = event.data().orderId();
        SagaInstanceEntity saga = loadSagaOrThrow(orderId);

        if (isFinal(saga.getState())) return;

        requireState(saga, SagaState.INVENTORY_REQUESTED);

        orderInventoryUpdater.markInventoryFailed(orderId, event.data().reason());

        saga.transitionTo(SagaState.CANCELLED);
        saga.putUuid("inventoryReservationId", event.data().reservationId(), objectMapper);
        saga.putString("reason", event.data().reason(), objectMapper);

        sagaRepo.save(saga);
        markProcessed(event.eventId());
    }

    private boolean alreadyProcessed(UUID eventId) {
        try {
            processedRepo.save(ProcessedEventEntity.now(eventId));
            return false;
        } catch (DataIntegrityViolationException duplicate) {
            log.info("Skipping already processed eventId={}", eventId);
            return true;
        }
    }

    private SagaInstanceEntity loadSagaOrThrow(UUID orderId) {
        return sagaRepo.findBySagaTypeAndAggregateId(SAGA_TYPE, orderId.toString())
            .orElseThrow(() -> new IllegalStateException(
                "Saga not found for orderId=" + orderId + ", sagaType=" + SAGA_TYPE
            ));
    }

    private static boolean isFinal(SagaState state) {
        return state == SagaState.PAID || state == SagaState.PAYMENT_FAILED || state == SagaState.CANCELLED;
    }

    private static void requireState(SagaInstanceEntity saga, SagaState expected) {
        if (saga.getState() != expected) {
            throw new IllegalStateException("Invalid saga state. Expected=" + expected +
                ", actual=" + saga.getState() +
                ", sagaId=" + saga.getSagaId());
        }
    }

    private void markProcessed(UUID eventId) {
        processedRepo.save(ProcessedEventEntity.now(eventId));
    }
}
