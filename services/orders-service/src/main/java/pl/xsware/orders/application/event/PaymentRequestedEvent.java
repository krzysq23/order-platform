package pl.xsware.orders.application.event;

import pl.xsware.orders.domain.order.Currency;
import pl.xsware.orders.domain.shared.OutboxEvent;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentRequestedEvent(
    UUID eventId,
    String eventType,
    int version,
    Instant occurredAt,
    Data data
) implements OutboxEvent {

    public static final String TYPE = "PaymentRequested";
    public static final int VERSION = 1;

    public static PaymentRequestedEvent of(
        UUID orderId,
        BigDecimal amount,
        Currency currency
    ) {
        return new PaymentRequestedEvent(
            UUID.randomUUID(),
            TYPE,
            VERSION,
            Instant.now(),
            new Data(orderId, amount, currency)
        );
    }

    @Override
    public String aggregateType() {
        return "ORDER";
    }

    @Override
    public String aggregateId() {
        return data.orderId().toString();
    }

    public record Data(
        UUID orderId,
        BigDecimal amount,
        Currency currency
    ) {}
}
