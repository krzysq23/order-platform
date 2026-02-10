package pl.xsware.orders.application.outbox;

import pl.xsware.orders.application.event.PaymentRequestedEvent;

public interface OutboxPublisher {

    void publish(PaymentRequestedEvent event);
}
