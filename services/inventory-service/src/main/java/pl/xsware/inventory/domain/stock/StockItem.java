package pl.xsware.inventory.domain.stock;

import lombok.Getter;
import pl.xsware.inventory.domain.stock.vo.Quantity;
import pl.xsware.inventory.domain.stock.vo.Sku;

import java.util.UUID;

@Getter
public class StockItem {

    private final UUID id;
    private final Sku sku;
    private final String warehouse;
    private int onHand;
    private int reserved;

    public StockItem(UUID id, Sku sku, String warehouse, int onHand, int reserved) {
        if (onHand < 0 || reserved < 0 || reserved > onHand) throw new IllegalArgumentException("Invalid stock state");
        this.id = id;
        this.sku = sku;
        this.warehouse = warehouse;
        this.onHand = onHand;
        this.reserved = reserved;
    }

    public int available() {
        return onHand - reserved;
    }

    public void reserve(Quantity qty) {
        if (available() < qty.value()) throw new NotEnoughStockException(sku.value(), warehouse, qty.value(), available());
        reserved += qty.value();
    }

    public void release(Quantity qty) {
        if (reserved < qty.value()) throw new IllegalStateException("Cannot release more than reserved");
        reserved -= qty.value();
    }

}
