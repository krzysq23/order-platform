package pl.xsware.orders.application.event;

import lombok.Value;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Value
@Builder
public class PaymentCancelledEvent {
    UUID eventId;
    String eventType;
    int version;
    Instant occurredAt;
    Data data;

    public static final String TYPE = "PaymentCancelled";
    public static final int VERSION = 1;

    @Value
    @Builder
    public static class Data {
        UUID orderId;
        UUID paymentId;
        String reason;
    }
}
