package pl.xsware.orders.application.event;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.UUID;

@Value
@Builder
public class PaymentSucceededEvent {
    UUID eventId;
    String eventType;
    int version;
    Instant occurredAt;
    Data data;

    public static final String TYPE = "PaymentSucceeded";
    public static final int VERSION = 1;

    @Value
    @Builder
    public static class Data {
        UUID orderId;
        UUID paymentId;
        String provider;
        String externalId;
    }
}
