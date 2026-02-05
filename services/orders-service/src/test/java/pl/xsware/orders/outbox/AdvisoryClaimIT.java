package pl.xsware.orders.outbox;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import pl.xsware.orders.application.outbox.OutboxClaimStrategy;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(properties = {
    "orders.outbox.claim-mode=ADVISORY"
})
class AdvisoryClaimIT extends OutboxClaimITBase {

    @Autowired
    OutboxClaimStrategy claimStrategy;

    @Test
    void shouldClaimWithoutDuplicatesAcrossTwoWorkers() throws Exception {
        insertOutboxMessages(200);

        int batchSize = 50;
        Duration lockTimeout = Duration.ofMinutes(2);

        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            Future<List<UUID>> f1 = pool.submit(() -> claimStrategy.claimNextBatch(batchSize, lockTimeout, "worker-A"));
            Future<List<UUID>> f2 = pool.submit(() -> claimStrategy.claimNextBatch(batchSize, lockTimeout, "worker-B"));

            List<UUID> a = f1.get(10, TimeUnit.SECONDS);
            List<UUID> b = f2.get(10, TimeUnit.SECONDS);

            Set<UUID> intersection = new HashSet<>(a);
            intersection.retainAll(b);

            assertThat(intersection).isEmpty();
            assertThat(a).hasSizeLessThanOrEqualTo(batchSize);
            assertThat(b).hasSizeLessThanOrEqualTo(batchSize);

            Set<UUID> union = new HashSet<>(a);
            union.addAll(b);
            assertThat(union.size()).isEqualTo(a.size() + b.size());

        } finally {
            pool.shutdownNow();
        }
    }
}
