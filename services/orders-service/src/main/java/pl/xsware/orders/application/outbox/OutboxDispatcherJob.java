package pl.xsware.orders.application.outbox;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxDispatcherJob {

    private final OutboxDispatcher dispatcher;

    @Scheduled(
        fixedDelayString = "${orders.outbox.dispatcher.fixed-delay:PT2S}",
        initialDelayString = "${orders.outbox.dispatcher.initial-delay:PT5S}"
    )
    public void run() {

        String lockedBy = instanceId();

        log.debug("OutboxDispatcherJob run instance={} ...", lockedBy);

        int batchSize = 50;
        Duration lockTimeout = Duration.ofMinutes(2);
        int maxBatchesPerRun = 10;

        for (int i = 0; i < maxBatchesPerRun; i++) {
            List<UUID> ids = dispatcher.claimBatch(batchSize, lockTimeout, lockedBy);
            if (ids.isEmpty()) return;

            for (UUID id : ids) {
                dispatcher.processOne(id);
            }
        }
    }

    private String instanceId() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            return "unknown-instance";
        }
    }
}
