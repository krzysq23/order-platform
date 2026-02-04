package pl.xsware.orders.api.order;

import java.time.Instant;
import java.util.UUID;

public record OrderResponse(
    UUID id,
    String status,
    Instant createdAt
) {}
