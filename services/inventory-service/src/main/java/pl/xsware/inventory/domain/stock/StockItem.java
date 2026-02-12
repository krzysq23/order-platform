package pl.xsware.inventory.domain.stock;

import lombok.Getter;
import pl.xsware.inventory.domain.stock.vo.Quantity;
import pl.xsware.inventory.domain.stock.vo.Sku;

import java.util.Objects;
import java.util.UUID;

@Getter
public class StockItem {

    private final UUID id;
    private final Sku sku;
    private final String warehouse;

    private int onHand;
    private int reserved;

    public StockItem(UUID id, Sku sku, String warehouse, int onHand, int reserved) {
        this.id = Objects.requireNonNull(id);
        this.sku = Objects.requireNonNull(sku);
        this.warehouse = Objects.requireNonNull(warehouse);
        if (onHand < 0 || reserved < 0 || reserved > onHand) throw new IllegalArgumentException("Invalid stock state");
        this.onHand = onHand;
        this.reserved = reserved;
    }

    public int available() { return onHand - reserved; }

    public void reserve(Quantity qty) {
        if (available() < qty.value()) {
            throw new NotEnoughStockException(sku.value(), warehouse, qty.value(), available());
        }
        reserved += qty.value();
    }

}
