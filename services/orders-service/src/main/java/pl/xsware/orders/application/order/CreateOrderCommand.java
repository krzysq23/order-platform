package pl.xsware.orders.application.order;

import java.math.BigDecimal;
import java.util.List;

public record CreateOrderCommand(
    String customerId,
    BigDecimal totalAmount,
    List<Item> items
) {

    public CreateOrderCommand {
        if (customerId == null || customerId.isBlank()) {
            throw new IllegalArgumentException("customerId must not be blank");
        }
        if (totalAmount == null) {
            throw new IllegalArgumentException("totalAmount must not be null");
        }
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("items must not be empty");
        }
    }

    public record Item(
        String sku,
        int quantity
    ) {
        public Item {
            if (sku == null || sku.isBlank()) {
                throw new IllegalArgumentException("sku must not be blank");
            }
            if (quantity <= 0) {
                throw new IllegalArgumentException("quantity must be > 0");
            }
        }
    }
}
