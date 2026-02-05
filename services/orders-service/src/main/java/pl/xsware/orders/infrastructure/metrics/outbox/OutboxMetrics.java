package pl.xsware.orders.infrastructure.metrics.outbox;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Component
public class OutboxMetrics {

    private final Counter successCounter;
    private final Counter retryCounter;
    private final Timer processTimer;

    @Getter
    private final AtomicLong lagSeconds = new AtomicLong(0);

    public OutboxMetrics(MeterRegistry registry) {
        this.successCounter = Counter.builder("outbox_dispatch_success_total")
            .description("Number of successfully processed outbox messages")
            .register(registry);

        this.retryCounter = Counter.builder("outbox_dispatch_retry_total")
            .description("Number of outbox messages scheduled for retry")
            .register(registry);

        this.processTimer = Timer.builder("outbox_dispatch_process_seconds")
            .description("Time spent processing a single outbox message")
            .publishPercentileHistogram()
            .register(registry);

        Gauge.builder("outbox_lag_seconds", lagSeconds, AtomicLong::get)
            .description("Lag of the oldest unprocessed outbox message in seconds")
            .register(registry);
    }

    public void markSuccess() {
        successCounter.increment();
    }

    public void markRetry() {
        retryCounter.increment();
    }

    public <T> T recordProcessTime(java.util.concurrent.Callable<T> callable) throws Exception {
        return processTimer.recordCallable(callable);
    }

    public void recordProcessTime(Runnable runnable) {
        processTimer.record(runnable);
    }

    public void setLagSeconds(long seconds) {
        lagSeconds.set(Math.max(0, seconds));
    }
}
