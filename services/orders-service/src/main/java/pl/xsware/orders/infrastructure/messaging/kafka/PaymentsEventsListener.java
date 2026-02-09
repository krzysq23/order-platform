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

    @KafkaListener(
        topics = "${app.kafka.topics.payment-succeeded}",
        groupId = "order-service"
    )
    public void onPaymentSucceeded(PaymentSucceededEvent event) {;
        log.info("PaymentSucceeded eventId={}, orderId={}", event.eventId(), event.data().orderId());
        sagaService.handle(event);
    }

    @KafkaListener(
        topics = "${app.kafka.topics.payment-failed}",
        groupId = "order-service"
    )
    public void onPaymentFailed(PaymentFailedEvent event) {
        log.info("PaymentFailed eventId={}, orderId={}", event.eventId(), event.data().orderId());
        sagaService.handle(event);
    }

    @KafkaListener(
        topics = "${app.kafka.topics.payment-cancelled}",
        groupId = "order-service"
    )
    public void onPaymentCancelled(PaymentCancelledEvent event) {
        log.info("PaymentCancelled eventId={}, orderId={}", event.eventId(), event.data().orderId());
        sagaService.handle(event);
    }

}
