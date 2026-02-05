package pl.xsware.orders.application.outbox;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pl.xsware.orders.infrastructure.metrics.outbox.OutboxMetrics;
import pl.xsware.orders.infrastructure.persistence.outbox.OutboxLagRepository;

import java.net.InetAddress;
import java.time.*;
import java.util.List;
import java.util.UUID;

@Slf4j(topic = "OUTBOX")
@ConditionalOnProperty(name="orders.outbox.dispatcher.enabled", havingValue="true", matchIfMissing=true)
@Component
@RequiredArgsConstructor
public class OutboxDispatcherJob {

    private final OutboxDispatcher dispatcher;
    private final OutboxMetrics metrics;
    private final OutboxLagRepository lagRepository;
    private final Clock clock;

    @Scheduled(
        fixedDelayString = "${orders.outbox.dispatcher.fixed-delay:PT2S}",
        initialDelayString = "${orders.outbox.dispatcher.initial-delay:PT5S}"
    )
    public void run() {

        String lockedBy = instanceId();

        log.debug("OUTBOX_JOB_RUN instance={} ...", lockedBy);

        OffsetDateTime now = OffsetDateTime.ofInstant(Instant.now(clock), ZoneOffset.UTC);
        long lag = lagRepository.computeLagSeconds(now);
        metrics.setLagSeconds(lag);

        int batchSize = 50;
        Duration lockTimeout = Duration.ofMinutes(2);
        int maxBatchesPerRun = 10;
        int totalClaimed = 0;
        int totalProcessed = 0;

        for (int i = 0; i < maxBatchesPerRun; i++) {
            List<UUID> ids = dispatcher.claimBatch(batchSize, lockTimeout, lockedBy);

            if (ids.isEmpty()) return;


            totalClaimed += ids.size();
            int processedThisBatch = 0;
            int processedSuccess = 0;
            int processedFailed = 0;

            for (UUID id : ids) {
                boolean isProcessed = dispatcher.processOne(id);
                processedThisBatch++;
                if (isProcessed) processedSuccess++; else processedFailed++;
            }

            totalProcessed += processedThisBatch;

            log.info("OUTBOX_JOB_BATCH claimed={} processed={}, processedSuccess={}, processedFailed={}, lockedBy={}",
                ids.size(), processedThisBatch, processedSuccess, processedFailed, lockedBy);
        }

        log.info("OUTBOX_JOB_DONE totalClaimed={} totalProcessed={} lockedBy={}",
            totalClaimed, totalProcessed, lockedBy);
    }

    private String instanceId() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            return "unknown-instance";
        }
    }
}
