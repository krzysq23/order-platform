package pl.xsware.orders.application.event;

import pl.xsware.orders.domain.order.Currency;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public final class PaymentRequestedEventFactory {

    public static PaymentRequestedEvent create(
        UUID orderId,
        BigDecimal amount,
        Currency currency
    ) {
        return new PaymentRequestedEvent(
            UUID.randomUUID(),
            PaymentRequestedEvent.TYPE,
            PaymentRequestedEvent.VERSION,
            Instant.now(),
            new PaymentRequestedEvent.Data(
                orderId,
                amount,
                currency
            )
        );
    }
}
