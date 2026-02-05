package pl.xsware.orders.api.outbox;

import java.time.Instant;
import java.util.UUID;

public record OutboxMessageResponse(
    UUID id,
    String aggregateType,
    String aggregateId,
    String eventType,
    Instant occurredAt,
    Instant createdAt,
    Instant processedAt
) {}
