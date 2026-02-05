package pl.xsware.orders.application.outbox;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.xsware.orders.infrastructure.persistence.outbox.OutboxJpaRepository;
import pl.xsware.orders.infrastructure.persistence.outbox.OutboxMessageEntity;

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

    @Transactional
    public List<UUID> claimBatch(int batchSize, Duration lockTimeout, String lockedBy) {
        return claimStrategy.claimNextBatch(batchSize, lockTimeout, lockedBy);
    }

    @Transactional
    public boolean processOne(UUID id) {
        Instant now = Instant.now(clock);

        OutboxMessageEntity msg = outboxJpaRepository.findById(id)
            .orElseThrow(() -> new IllegalStateException("Claimed outbox message missing: " + id));

        if (msg.getProcessedAt() != null) {
            clearLock(msg);
            outboxJpaRepository.save(msg);
            return true;
        }

        try {
            publisher.publish(msg.getId().toString(), msg.getEventType(), msg.getPayload());

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

            log.warn("Outbox failed id={} attempts={} nextAttemptAt={} error={}",
                id, nextAttempts, msg.getNextAttemptAt(), e.toString(), e);
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
