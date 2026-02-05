package pl.xsware.orders.infrastructure.logging.outbox;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pl.xsware.orders.application.outbox.OutboxPublisher;

@Slf4j
@Component
public class LoggingOutboxPublisher implements OutboxPublisher {

    @Override
    public void publish(String messageId, String type, String payload) {
        log.info("OUTBOX_PUBLISH id={} type={} payload={}", messageId, type, payload);
    }
}
