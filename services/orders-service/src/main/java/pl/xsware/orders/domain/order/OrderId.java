package pl.xsware.orders.domain.order;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.UUID;

@EqualsAndHashCode
@ToString
public class OrderId {

    private final UUID value;

    private OrderId(UUID value) {
        this.value = value;
    }

    public static OrderId newId() {
        return new OrderId(UUID.randomUUID());
    }

    public static OrderId of(UUID value) {
        return new OrderId(value);
    }

    public UUID value() {
        return value;
    }
}
