package pl.xsware.orders.infrastructure.messaging.kafka.utils;

public class PayloadDeserializationException extends RuntimeException {

    public PayloadDeserializationException(
        String topic,
        Class<?> targetType,
        String payload,
        Throwable cause
    ) {
        super(
            "Failed to deserialize payload from topic='%s' to type='%s'"
                .formatted(topic, targetType.getSimpleName()),
            cause
        );
    }
}
