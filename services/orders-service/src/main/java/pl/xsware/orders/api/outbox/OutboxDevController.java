package pl.xsware.orders.api.outbox;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.xsware.orders.infrastructure.persistence.outbox.OutboxJpaRepository;

import java.util.List;

@Profile("dev")
@RestController
@RequestMapping("/dev/outbox")
@RequiredArgsConstructor
public class OutboxDevController {

    private final OutboxJpaRepository outboxJpaRepository;

    @GetMapping("/unprocessed")
    public List<OutboxMessageResponse> getUnprocessed(@RequestParam(defaultValue = "20") int limit) {

        int safeLimit = Math.min(Math.max(limit, 1), 200);

        return outboxJpaRepository
            .findByProcessedAtIsNullOrderByCreatedAtDesc(PageRequest.of(0, safeLimit))
            .stream()
            .map(e -> new OutboxMessageResponse(
                e.getId(),
                e.getAggregateType(),
                e.getAggregateId(),
                e.getEventType(),
                e.getOccurredAt(),
                e.getCreatedAt(),
                e.getProcessedAt()
            ))
            .toList();
    }
}
