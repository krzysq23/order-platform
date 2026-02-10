package pl.xsware.inventory.domain.reservation;

import pl.xsware.inventory.domain.stock.vo.Quantity;
import pl.xsware.inventory.domain.stock.vo.Sku;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record ReserveStockCommand(
    UUID orderId,
    UUID correlationId,
    Instant expiresAt,
    List<Item> items
) {
    public ReserveStockCommand {
        Objects.requireNonNull(orderId, "orderId");
        Objects.requireNonNull(items, "items");
        if (items.isEmpty()) throw new IllegalArgumentException("items cannot be empty");
    }

    public record Item(Sku sku, String warehouse, Quantity quantity) {
        public Item {
            Objects.requireNonNull(sku, "sku");
            Objects.requireNonNull(warehouse, "warehouse");
            Objects.requireNonNull(quantity, "quantity");
            var w = warehouse.trim();
            if (w.isEmpty() || w.length() > 32) throw new IllegalArgumentException("Invalid warehouse");
            warehouse = w;
        }
    }
}
