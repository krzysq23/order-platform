package pl.xsware.orders.infrastructure.messaging.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import pl.xsware.orders.application.event.PaymentCancelledEvent;
import pl.xsware.orders.application.event.PaymentFailedEvent;
import pl.xsware.orders.application.event.PaymentSucceededEvent;
import pl.xsware.orders.application.saga.OrderPaymentSagaService;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.exc.JsonNodeException;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentsEventsListener {

    private final OrderPaymentSagaService sagaService;
    private final ObjectMapper objectMapper;

    @KafkaListener(
        topics = "${app.kafka.topics.payment-succeeded}",
        groupId = "order-service"
    )
    public void onPaymentSucceeded(String payload) {
        PaymentSucceededEvent event = deserialize(payload, PaymentSucceededEvent.class, "onPaymentSucceeded");
        log.info("PaymentSucceeded eventId={}, orderId={}", event.eventId(), event.data().orderId());
        sagaService.handle(event);
    }

    @KafkaListener(
        topics = "${app.kafka.topics.payment-failed}",
        groupId = "order-service"
    )
    public void onPaymentFailed(String payload) {
        PaymentFailedEvent event = deserialize(payload, PaymentFailedEvent.class, "onPaymentFailed");
        log.info("PaymentFailed eventId={}, orderId={}", event.eventId(), event.data().orderId());
        sagaService.handle(event);
    }

    @KafkaListener(
        topics = "${app.kafka.topics.payment-cancelled}",
        groupId = "order-service"
    )
    public void onPaymentCancelled(String payload) {
        PaymentCancelledEvent event = deserialize(payload, PaymentCancelledEvent.class, "onPaymentCancelled");
        log.info("PaymentCancelled eventId={}, orderId={}", event.eventId(), event.data().orderId());
        sagaService.handle(event);
    }

    private <T> T deserialize(String payload, Class<T> type, String topicName) {
        try {
            return objectMapper.readValue(payload, type);
        } catch (JsonNodeException e) {
            log.error("Cannot deserialize payload from topic={} type={} payload={}", topicName, type.getSimpleName(), payload, e);
            throw new IllegalStateException("Bad message on " + topicName + " for type " + type.getName(), e);
        }
    }
}
