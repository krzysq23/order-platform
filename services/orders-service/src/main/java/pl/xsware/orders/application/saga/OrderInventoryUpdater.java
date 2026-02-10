package pl.xsware.orders.application.saga;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.xsware.orders.application.event.StockReservedEvent;
import pl.xsware.orders.domain.order.Order;
import pl.xsware.orders.domain.order.OrderId;
import pl.xsware.orders.domain.order.OrderRepository;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderInventoryUpdater {

    private final OrderRepository orderRepository;

    @Transactional
    public void markInventoryReserved(UUID orderUuid, StockReservedEvent.Data data) {

        OrderId orderId = OrderId.of(orderUuid);

        Order order = orderRepository.findById(orderId)
            .orElseThrow(() ->
                new IllegalStateException("Order not found: " + orderUuid));

        try {
            order.startPayment();
        } catch (IllegalStateException ex) {
            log.warn(
                "Ignoring inventory reserved due to invalid order state. orderId={}, status={}",
                orderId.value(), order.getStatus()
            );
            return;
        }

        orderRepository.save(order);

        log.info(
            "Inventory reserved for order. orderId={}, reservationId={}, newStatus={}",
            orderId.value(),
            data.reservationId(),
            order.getStatus()
        );
    }

    @Transactional
    public void markInventoryFailed(UUID orderUuid, String reason) {

        OrderId orderId = OrderId.of(orderUuid);

        Order order = orderRepository.findById(orderId)
            .orElseThrow(() ->
                new IllegalStateException("Order not found: " + orderUuid));

        try {
            order.cancel(reason);
        } catch (IllegalStateException ex) {
            log.warn(
                "Ignoring inventory failure due to invalid order state. orderId={}, status={}",
                orderId.value(), order.getStatus()
            );
            return;
        }

        orderRepository.save(order);

        log.warn(
            "Inventory failed for order. orderId={}, reason={}, newStatus={}",
            orderId.value(), reason, order.getStatus()
        );
    }
}
