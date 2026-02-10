package pl.xsware.orders.infrastructure.messaging.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pl.xsware.orders.application.event.PaymentRequestedEvent;
import pl.xsware.orders.application.outbox.OutboxPublisher;

@Slf4j(topic = "OUTBOX")
@RequiredArgsConstructor
@Component
public class PaymentRequestedKafkaPublisherImpl implements OutboxPublisher {

    private final PaymentRequestedKafkaPublisher paymentPublisher;

    @Override
    public void publish(PaymentRequestedEvent event) {

        paymentPublisher.publish(event);

        log.info("OUTBOX_PUBLISH_PAYMENT_REQUEST eventId={} type={} payload={}", event.eventId(), event.eventType(), event.data().toString());
    }
}
