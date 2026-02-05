package pl.xsware.orders.application.outbox;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

public interface OutboxClaimStrategy {

    List<UUID> claimNextBatch(int batchSize, Duration lockTimeout, String lockedBy);
}
