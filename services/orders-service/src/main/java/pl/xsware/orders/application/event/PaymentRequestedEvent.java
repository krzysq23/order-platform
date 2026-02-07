package pl.xsware.orders.application.event;

import lombok.Builder;
import lombok.Value;
import pl.xsware.orders.domain.order.Currency;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Value
@Builder
public class PaymentRequestedEvent {

    UUID eventId;
    String eventType;
    int version;
    Instant occurredAt;
    Data data;

    public static final String TYPE = "PaymentRequested";
    public static final int VERSION = 1;

    @Value
    @Builder
    public static class Data {
        UUID orderId;
        BigDecimal amount;
        Currency currency;
    }
}
