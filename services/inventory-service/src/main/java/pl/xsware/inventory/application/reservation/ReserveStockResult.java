package pl.xsware.inventory.application.reservation;

import java.util.UUID;

public sealed interface ReserveStockResult
    permits ReserveStockResult.AlreadyProcessed,
            ReserveStockResult.Reserved,
            ReserveStockResult.Failed {

    record AlreadyProcessed(UUID eventId)
            implements ReserveStockResult {}

    record Reserved(UUID orderId, UUID reservationId)
            implements ReserveStockResult {}

    record Failed(UUID orderId, UUID reservationId, String reason)
            implements ReserveStockResult {}
}
