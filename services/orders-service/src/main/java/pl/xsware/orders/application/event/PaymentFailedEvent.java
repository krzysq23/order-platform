package pl.xsware.orders.application.event;

import lombok.Value;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Value
@Builder
public class PaymentFailedEvent {
    UUID eventId;
    String eventType;
    int version;
    Instant occurredAt;
    Data data;

    public static final String TYPE = "PaymentFailed";
    public static final int VERSION = 1;

    @Value
    @Builder
    public static class Data {
        UUID orderId;
        UUID paymentId;
        String reason;
    }
}
