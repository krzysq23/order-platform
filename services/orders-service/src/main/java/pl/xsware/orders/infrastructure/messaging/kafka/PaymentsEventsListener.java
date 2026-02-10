package pl.xsware.orders.infrastructure.messaging.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import pl.xsware.orders.application.event.PaymentCancelledEvent;
import pl.xsware.orders.application.event.PaymentFailedEvent;
import pl.xsware.orders.application.event.PaymentSucceededEvent;
import pl.xsware.orders.application.saga.OrderPaymentSagaService;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentsEventsListener {

    private final OrderPaymentSagaService sagaService;
    private final PayloadDataUtils payloadDataUtils;

    @KafkaListener(
        topics = "${app.kafka.topics.payment-succeeded}",
        groupId = "order-service"
    )
    public void onPaymentSucceeded(String payload) {
        PaymentSucceededEvent event = payloadDataUtils.deserialize(payload, PaymentSucceededEvent.class, "onPaymentSucceeded");
        log.info("PaymentSucceeded eventId={}, orderId={}", event.eventId(), event.data().orderId());
        sagaService.handle(event);
    }

    @KafkaListener(
        topics = "${app.kafka.topics.payment-failed}",
        groupId = "order-service"
    )
    public void onPaymentFailed(String payload) {
        PaymentFailedEvent event = payloadDataUtils.deserialize(payload, PaymentFailedEvent.class, "onPaymentFailed");
        log.info("PaymentFailed eventId={}, orderId={}", event.eventId(), event.data().orderId());
        sagaService.handle(event);
    }

    @KafkaListener(
        topics = "${app.kafka.topics.payment-cancelled}",
        groupId = "order-service"
    )
    public void onPaymentCancelled(String payload) {
        PaymentCancelledEvent event = payloadDataUtils.deserialize(payload, PaymentCancelledEvent.class, "onPaymentCancelled");
        log.info("PaymentCancelled eventId={}, orderId={}", event.eventId(), event.data().orderId());
        sagaService.handle(event);
    }

}
