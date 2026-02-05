package pl.xsware.orders.application.outbox;

public interface OutboxPublisher {

    void publish(String messageId, String eventType, String payload);
}
