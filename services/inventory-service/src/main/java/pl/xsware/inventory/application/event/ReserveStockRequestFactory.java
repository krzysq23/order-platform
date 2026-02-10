package pl.xsware.inventory.application.event;

import pl.xsware.inventory.application.reservation.ReserveStockRequest;

public class ReserveStockRequestFactory {

    public static ReserveStockRequest create(ReserveStockRequestedEvent event) {
        return new ReserveStockRequest(
            event.eventId(),
            event.eventType(),
            event.occurredAt(),
            event.data().orderId(),
            event.data().correlationId(),
            event.data().expiresAt(),
            event.data().items().stream()
                .map(i -> new ReserveStockRequest.Item(i.sku(), i.quantity()))
                .toList()
        );
    }
}
