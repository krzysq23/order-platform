package pl.xsware.orders.application.outbox;

import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class OutboxRetryPolicy {

    public Duration nextDelay(int attempts) {
        long seconds = Math.min(300, 1L << Math.min(attempts - 1, 16));
        return Duration.ofSeconds(seconds);
    }
}
