package pl.xsware.orders.application.event;

import pl.xsware.orders.domain.order.Currency;

import java.time.Instant;
import java.util.UUID;

public class PaymentRequestedEventFactory {

    public static PaymentRequestedEvent create(UUID orderId, java.math.BigDecimal amount, Currency currency) {
        return PaymentRequestedEvent.builder()
            .eventId(UUID.randomUUID())
            .eventType(PaymentRequestedEvent.TYPE)
            .version(PaymentRequestedEvent.VERSION)
            .occurredAt(Instant.now())
            .data(PaymentRequestedEvent.Data.builder()
                .orderId(orderId)
                .amount(amount)
                .currency(currency)
                .build())
            .build();
    }
}
