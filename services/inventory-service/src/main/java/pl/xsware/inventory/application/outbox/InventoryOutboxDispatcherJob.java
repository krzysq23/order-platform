package pl.xsware.inventory.application.outbox;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pl.xsware.inventory.infrastructure.messaging.kafka.InventoryKafkaPublisher;
import pl.xsware.inventory.infrastructure.persistance.outbox.entity.OutboxMessageEntity;
import pl.xsware.inventory.infrastructure.persistance.outbox.repository.OutboxMessageJpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j(topic = "OUTBOX")
@ConditionalOnProperty(name="orders.outbox.dispatcher.enabled", havingValue="true", matchIfMissing=true)
@Component
@RequiredArgsConstructor
public class InventoryOutboxDispatcherJob {

    private static final int BATCH_SIZE = 20;
    private static final String LOCK_OWNER = "inventory-outbox";

    private final OutboxMessageJpaRepository outboxRepo;
    private final InventoryKafkaPublisher publisher;

    @Scheduled(
        fixedDelayString = "${orders.outbox.dispatcher.fixed-delay:PT2S}",
        initialDelayString = "${orders.outbox.dispatcher.initial-delay:PT5S}"
    )
    public void dispatch() {

        List<OutboxMessageEntity> batch = claimBatch();
        if (batch.isEmpty()) {
            return;
        }

        log.debug("Outbox batch claimed. size={}", batch.size());

        for (OutboxMessageEntity msg : batch) {
            try {
                publisher.publish(msg);
                markProcessed(msg.getId());
            } catch (Exception ex) {
                markFailed(msg.getId(), ex);
            }
        }
    }

    @Transactional
    protected List<OutboxMessageEntity> claimBatch() {
        return outboxRepo.claimBatch(
            Instant.now(),
            LOCK_OWNER + "-" + UUID.randomUUID(),
            BATCH_SIZE,
            null
        );
    }

    @Transactional
    protected void markProcessed(UUID id) {
        outboxRepo.markProcessed(id, Instant.now());
    }

    @Transactional
    protected void markFailed(UUID id, Exception ex) {
        log.error("Outbox publish failed. messageId={}", id, ex);
        Instant nextAttempt = Instant.now().plusSeconds(30);
        outboxRepo.markFailed(id, nextAttempt, ex.getMessage());
    }
}
