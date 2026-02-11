package pl.xsware.orders.infrastructure.messaging.kafka.inventory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import pl.xsware.orders.application.event.StockReservationFailedEvent;
import pl.xsware.orders.application.event.StockReservedEvent;
import pl.xsware.orders.application.saga.OrderPaymentSagaService;
import pl.xsware.orders.infrastructure.messaging.kafka.utils.PayloadDataUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryEventsListener {

    private final OrderPaymentSagaService sagaService;
    private final PayloadDataUtils payloadDataUtils;

    @KafkaListener(
        topics = "${app.kafka.topics.stock-reserved}",
        groupId = "order-service"
    )
    public void onStockReserved(String payload) {
        StockReservedEvent event = payloadDataUtils.deserialize(payload, StockReservedEvent.class, "onStockReserved");
        log.info("Inventory stock-reserved received eventId={}, orderId={}", event.eventId(), event.data().orderId());
        sagaService.handle(event);
    }

    @KafkaListener(
        topics = "${app.kafka.topics.stock-reservation-failed}",
        groupId = "order-service"
    )
    public void onStockReservationFailed(String payload) {
        StockReservationFailedEvent event = payloadDataUtils.deserialize(payload, StockReservationFailedEvent.class, "onStockReservationFailed");
        log.info("Inventory stock-reservation-failed received eventId={}, orderId={}", event.eventId(), event.data().orderId());
        sagaService.handle(event);
    }

}
