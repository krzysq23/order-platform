package pl.xsware.orders.application.outbox;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.xsware.orders.application.event.PaymentRequestedEvent;
import pl.xsware.orders.application.event.ReserveStockRequestedEvent;
import pl.xsware.orders.application.saga.OrderPaymentSagaService;
import pl.xsware.orders.infrastructure.metrics.outbox.OutboxMetrics;
import pl.xsware.orders.infrastructure.persistence.outbox.OutboxJpaRepository;
import pl.xsware.orders.infrastructure.persistence.outbox.OutboxMessageEntity;
import tools.jackson.databind.ObjectMapper;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j(topic = "OUTBOX")
@Service
@RequiredArgsConstructor
public class OutboxDispatcher {


    private final OutboxClaimStrategy claimStrategy;
    private final OutboxJpaRepository outboxJpaRepository;
    private final OutboxPublisher publisher;
    private final OutboxRetryPolicy retryPolicy;
    private final Clock clock;
    private final OutboxMetrics metrics;
    private final ObjectMapper objectMapper;
    private final String sagaType = OrderPaymentSagaService.SAGA_TYPE;

    @Transactional
    public List<UUID> claimBatch(int batchSize, Duration lockTimeout, String lockedBy) {
        return claimStrategy.claimNextBatch(batchSize, lockTimeout, lockedBy);
    }

    @Transactional
    public boolean processOne(UUID id) {
        final boolean[] result = new boolean[1];

        metrics.recordProcessTime(() -> {
            result[0] = doProcessOne(id);
        });

        return result[0];
    }

    @Transactional
    public boolean doProcessOne(UUID id) {

        Instant now = Instant.now(clock);

        OutboxMessageEntity msg = outboxJpaRepository.findById(id)
            .orElseThrow(() -> new IllegalStateException("Claimed outbox message missing: " + id));

        if (msg.getProcessedAt() != null) {
            clearLock(msg);
            outboxJpaRepository.save(msg);
            return true;
        }

        log.info("OUTBOX_PROCESSING started outboxId={} eventType={} aggregateId={} attempts={}",
            id,
            msg.getEventType(),
            msg.getAggregateId(),
            msg.getAttempts()
        );

        try {

            String eventType = msg.getEventType();

            switch (eventType) {

                case PaymentRequestedEvent.TYPE -> {
                    PaymentRequestedEvent event =
                        objectMapper.readValue(msg.getPayload(), PaymentRequestedEvent.class);

                    if (event.data() == null || event.data().orderId() == null) {
                        throw new IllegalStateException(
                            "Invalid PaymentRequestedEvent payload: data/orderId is null, outboxId=" + id
                        );
                    }

                    publisher.publish(event);
                }

                case ReserveStockRequestedEvent.TYPE -> {
                    ReserveStockRequestedEvent event =
                        objectMapper.readValue(msg.getPayload(), ReserveStockRequestedEvent.class);

                    if (event.data() == null || event.data().orderId() == null) {
                        throw new IllegalStateException(
                            "Invalid StockReservedEvent payload: data/orderId is null, outboxId=" + id
                        );
                    }

                    publisher.publish(event);
                }

                default -> throw new IllegalStateException(
                    "Unsupported outbox eventType for publisher: " + eventType + ", outboxId=" + id
                );
            }

            msg.setProcessedAt(now);
            msg.setLastError(null);
            clearLock(msg);
            outboxJpaRepository.save(msg);

            return true;

        } catch (Exception e) {
            int nextAttempts = msg.getAttempts() + 1;
            msg.setAttempts(nextAttempts);

            var delay = retryPolicy.nextDelay(nextAttempts);
            msg.setNextAttemptAt(now.plus(delay));
            msg.setLastError(trim(e.toString(), 4000));
            clearLock(msg);

            outboxJpaRepository.save(msg);

            log.warn("Outbox failed id={} type={} attempts={} nextAttemptAt={} error={}",
                id, msg.getEventType(), nextAttempts, msg.getNextAttemptAt(), e.toString(), e);

            return false;
        }
    }

    private void clearLock(OutboxMessageEntity msg) {
        msg.setLockedAt(null);
        msg.setLockedBy(null);
    }

    private String trim(String s, int max) {
        if (s == null) return null;
        return s.length() <= max ? s : s.substring(0, max);
    }
}
