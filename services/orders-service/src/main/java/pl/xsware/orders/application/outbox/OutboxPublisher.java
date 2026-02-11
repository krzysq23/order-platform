package pl.xsware.orders.application.outbox;

import pl.xsware.orders.application.event.PaymentRequestedEvent;
import pl.xsware.orders.application.event.ReserveStockRequestedEvent;

public interface OutboxPublisher {

    void publish(PaymentRequestedEvent event);
    void publish(ReserveStockRequestedEvent event);
}
