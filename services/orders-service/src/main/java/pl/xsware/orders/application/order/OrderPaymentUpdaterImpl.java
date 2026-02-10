package pl.xsware.orders.application.order;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.xsware.orders.application.outbox.OutboxWriter;
import pl.xsware.orders.domain.order.Order;
import pl.xsware.orders.domain.order.OrderId;
import pl.xsware.orders.domain.order.OrderRepository;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OrderPaymentUpdaterImpl implements OrderPaymentUpdater {

    private final OrderRepository orderRepository;
    private final OutboxWriter outboxWriter;

    @Override
    @Transactional
    public void markPaid(UUID orderId, UUID paymentId, String provider, String externalId) {
        Order order = orderRepository.findById(OrderId.of(orderId))
            .orElseThrow(() -> new IllegalStateException("Order not found: " + orderId));

        order.startPayment();

        orderRepository.save(order);

        outboxWriter.write(OrderPaidEvent.of(orderId));
    }

    @Override
    @Transactional
    public void markPaymentFailed(UUID orderId, String reason) {
        Order order = orderRepository.findById(OrderId.of(orderId))
            .orElseThrow(() -> new IllegalStateException("Order not found: " + orderId));

        order.cancel(reason != null ? reason : "PAYMENT_FAILED");

        orderRepository.save(order);
        outboxWriter.write(OrderPaymentFailedEvent.of(orderId, reason));
    }

    @Override
    @Transactional
    public void markPaymentCancelled(UUID orderId, String reason) {
        Order order = orderRepository.findById(OrderId.of(orderId))
            .orElseThrow(() -> new IllegalStateException("Order not found: " + orderId));

        order.cancel(reason != null ? reason : "PAYMENT_CANCELLED");

        orderRepository.save(order);
        outboxWriter.write(OrderCancelledEvent.of(orderId, reason));

    }
}
