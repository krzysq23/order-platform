package pl.xsware.orders.domain.order;

import lombok.Getter;
import pl.xsware.orders.domain.shared.DomainEvent;

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

    private final List<DomainEvent> domainEvents = new ArrayList<>();

    private Order(OrderId id, String customerId) {
        this.id = id;
        this.customerId = Objects.requireNonNull(customerId);
        this.status = OrderStatus.CREATED;
        this.createdAt = Instant.now();

        this.domainEvents.add(OrderCreatedEvent.now(this));
    }

    private Order(OrderId id, String customerId, OrderStatus status, Instant createdAt) {
        this.id = id;
        this.customerId = customerId;
        this.status = status;
        this.createdAt = createdAt;
    }

    public static Order create(String customerId) {
        return new Order(OrderId.newId(), customerId);
    }

    public static Order rehydrate(OrderId id, String customerId, OrderStatus status, Instant createdAt) {
        return new Order(id, customerId, status, createdAt);
    }

    public List<DomainEvent> pullDomainEvents() {
        List<DomainEvent> events = new ArrayList<>(domainEvents);
        domainEvents.clear();
        return events;
    }

    public List<DomainEvent> peekDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

}
