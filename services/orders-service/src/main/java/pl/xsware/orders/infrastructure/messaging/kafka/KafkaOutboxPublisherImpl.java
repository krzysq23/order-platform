package pl.xsware.orders.infrastructure.messaging.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pl.xsware.orders.application.event.PaymentRequestedEvent;
import pl.xsware.orders.application.event.ReserveStockRequestedEvent;
import pl.xsware.orders.application.outbox.OutboxPublisher;
import pl.xsware.orders.infrastructure.messaging.kafka.inventory.InventoryRequestedKafkaPublisher;
import pl.xsware.orders.infrastructure.messaging.kafka.payments.PaymentRequestedKafkaPublisher;

@Slf4j(topic = "OUTBOX")
@RequiredArgsConstructor
@Component
public class KafkaOutboxPublisherImpl implements OutboxPublisher {

    private final PaymentRequestedKafkaPublisher paymentPublisher;
    private final InventoryRequestedKafkaPublisher inventoryPublisher;

    @Override
    public void publish(PaymentRequestedEvent event) {

        paymentPublisher.publish(event);

        log.info("OUTBOX_PUBLISH_PAYMENT_REQUEST eventId={} type={} payload={}", event.eventId(), event.eventType(), event.data().toString());
    }

    @Override
    public void publish(ReserveStockRequestedEvent event) {

        inventoryPublisher.publish(event);

        log.info("OUTBOX_PUBLISH_STOCK_RESERVED eventId={} type={} payload={}", event.eventId(), event.eventType(), event.data().toString());
    }
}
