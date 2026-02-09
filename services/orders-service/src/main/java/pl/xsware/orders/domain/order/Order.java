package pl.xsware.orders.domain.order;

import lombok.Getter;
import pl.xsware.orders.application.order.OrderCancelledEvent;
import pl.xsware.orders.application.order.OrderPaidEvent;
import pl.xsware.orders.application.order.OrderPaymentFailedEvent;
import pl.xsware.orders.domain.shared.OutboxEvent;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Getter
public class Order {

    private final OrderId id;
    private final String customerId;
    private OrderStatus status;
    private final Instant createdAt;
    private final BigDecimal totalAmount;
    private final Currency currency;

    private final List<OutboxEvent> domainEvents = new ArrayList<>();

    private Order(OrderId id, String customerId, BigDecimal totalAmount, Currency currency) {
        this.id = Objects.requireNonNull(id);
        this.customerId = Objects.requireNonNull(customerId);
        this.status = OrderStatus.CREATED;
        this.createdAt = Instant.now();

        this.totalAmount = Objects.requireNonNull(totalAmount);
        this.currency = Objects.requireNonNull(currency);

        validateAmount(this.totalAmount);

        this.domainEvents.add(OrderCreatedEvent.now(this));
    }

    private Order(
        OrderId id,
        String customerId,
        OrderStatus status,
        Instant createdAt,
        BigDecimal totalAmount,
        Currency currency
    ) {
        this.id = Objects.requireNonNull(id);
        this.customerId = Objects.requireNonNull(customerId);
        this.status = Objects.requireNonNull(status);
        this.createdAt = Objects.requireNonNull(createdAt);

        this.totalAmount = Objects.requireNonNull(totalAmount);
        this.currency = Objects.requireNonNull(currency);

        validateAmount(this.totalAmount);
    }

    public static Order create(String customerId, BigDecimal totalAmount, Currency currency) {
        return new Order(OrderId.newId(), customerId, totalAmount, currency);
    }

    public static Order rehydrate(
        OrderId id,
        String customerId,
        OrderStatus status,
        Instant createdAt,
        BigDecimal totalAmount,
        Currency currency
    ) {
        return new Order(id, customerId, status, createdAt, totalAmount, currency);
    }

    public List<OutboxEvent> pullDomainEvents() {
        List<OutboxEvent> events = new ArrayList<>(domainEvents);
        domainEvents.clear();
        return events;
    }

    public List<OutboxEvent> peekDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    public void startPayment() {
        if (status == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Cannot start payment for cancelled order " + id.value());
        }
        if (status == OrderStatus.PAID) {
            return; // idempotent
        }
        if (status != OrderStatus.CREATED && status != OrderStatus.PAYMENT_FAILED) {
            throw new IllegalStateException("Invalid transition to PAYMENT_PENDING from " + status);
        }
        this.status = OrderStatus.PAYMENT_PENDING;
    }

    public void markPaid() {
        if (status == OrderStatus.PAID) {
            return;
        }
        if (status != OrderStatus.PAYMENT_PENDING) {
            throw new IllegalStateException("Invalid transition to PAID from " + status);
        }
        this.status = OrderStatus.PAID;

        this.domainEvents.add(OrderPaidEvent.now(this));
    }

    public void markPaymentFailed(String reason) {
        if (status == OrderStatus.PAYMENT_FAILED) {
            return;
        }
        if (status != OrderStatus.PAYMENT_PENDING) {
            throw new IllegalStateException("Invalid transition to PAYMENT_FAILED from " + status);
        }
        this.status = OrderStatus.PAYMENT_FAILED;
        this.domainEvents.add(OrderPaymentFailedEvent.now(this, reason));
    }

    public void cancel(String reason) {
        if (status == OrderStatus.CANCELLED) {
            return;
        }
        if (status == OrderStatus.PAID) {
            throw new IllegalStateException("Cannot cancel PAID order " + id.value());
        }
        if (status != OrderStatus.CREATED
            && status != OrderStatus.PAYMENT_PENDING
            && status != OrderStatus.PAYMENT_FAILED) {
            throw new IllegalStateException("Invalid transition to CANCELLED from " + status);
        }

        this.status = OrderStatus.CANCELLED;
        this.domainEvents.add(OrderCancelledEvent.now(this, reason));
    }

    private static void validateAmount(BigDecimal totalAmount) {
        if (totalAmount.signum() < 0) {
            throw new IllegalArgumentException("totalAmount must be >= 0");
        }
        if (totalAmount.scale() != 2) {
            throw new IllegalArgumentException("totalAmount must have scale=2");
        }
    }
}
