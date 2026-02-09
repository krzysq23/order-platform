package pl.xsware.orders.application.saga;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.xsware.orders.application.event.PaymentCancelledEvent;
import pl.xsware.orders.application.event.PaymentFailedEvent;
import pl.xsware.orders.application.event.PaymentSucceededEvent;
import pl.xsware.orders.application.order.OrderPaymentUpdater;
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

    private final OrderPaymentUpdater orderPaymentUpdater;
    private final ObjectMapper objectMapper;

    @Transactional
    public void handle(PaymentSucceededEvent event) {
        if (alreadyProcessed(event.getEventId())) return;

        UUID orderId = event.getData().getOrderId();
        SagaInstanceEntity saga = loadSagaOrThrow(orderId);

        if (isFinal(saga.getState())) return;

        requireState(saga, SagaState.PAYMENT_REQUESTED);

        orderPaymentUpdater.markPaid(
            orderId,
            event.getData().getPaymentId(),
            event.getData().getProvider(),
            event.getData().getExternalId()
        );

        saga.transitionTo(SagaState.PAID);
        saga.putUuid("paymentId", event.getData().getPaymentId(), objectMapper);
        saga.putString("provider", event.getData().getProvider(), objectMapper);
        saga.putString("externalId", event.getData().getExternalId(), objectMapper);

        sagaRepo.save(saga);
    }

    @Transactional
    public void handle(PaymentFailedEvent event) {
        if (alreadyProcessed(event.getEventId())) return;

        UUID orderId = event.getData().getOrderId();
        SagaInstanceEntity saga = loadSagaOrThrow(orderId);

        if (isFinal(saga.getState())) return;

        requireState(saga, SagaState.PAYMENT_REQUESTED);

        orderPaymentUpdater.markPaymentFailed(orderId, event.getData().getReason());

        saga.transitionTo(SagaState.FAILED);
        saga.putUuid("paymentId", event.getData().getPaymentId(), objectMapper);
        saga.putString("reason", event.getData().getReason(), objectMapper);

        sagaRepo.save(saga);
    }

    @Transactional
    public void handle(PaymentCancelledEvent event) {
        if (alreadyProcessed(event.getEventId())) return;

        UUID orderId = event.getData().getOrderId();
        SagaInstanceEntity saga = loadSagaOrThrow(orderId);

        if (isFinal(saga.getState())) return;

        requireState(saga, SagaState.PAYMENT_REQUESTED);

        orderPaymentUpdater.markPaymentCancelled(orderId, event.getData().getReason());

        saga.transitionTo(SagaState.CANCELLED);
        saga.putUuid("paymentId", event.getData().getPaymentId(), objectMapper);
        saga.putString("reason", event.getData().getReason(), objectMapper);

        sagaRepo.save(saga);
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
        return state == SagaState.PAID || state == SagaState.FAILED || state == SagaState.CANCELLED;
    }

    private static void requireState(SagaInstanceEntity saga, SagaState expected) {
        if (saga.getState() != expected) {
            throw new IllegalStateException("Invalid saga state. Expected=" + expected +
                ", actual=" + saga.getState() +
                ", sagaId=" + saga.getSagaId());
        }
    }
}
