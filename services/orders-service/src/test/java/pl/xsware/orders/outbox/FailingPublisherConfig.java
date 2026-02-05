package pl.xsware.orders.outbox;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import pl.xsware.orders.application.outbox.OutboxPublisher;

@TestConfiguration
public class FailingPublisherConfig {

    @Bean
    @Primary
    public OutboxPublisher failingOutboxPublisher() {
        return (messageId, eventType, payload) -> {
            throw new RuntimeException("Simulated publish failure");
        };
    }
}
