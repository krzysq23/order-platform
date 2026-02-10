package pl.xsware.inventory.domain.stock;

import lombok.Getter;

@Getter
public class NotEnoughStockException extends RuntimeException {

    private final String sku;
    private final String warehouse;
    private final int requested;
    private final int available;

    public NotEnoughStockException(String sku, String warehouse, int requested, int available) {
        super("Not enough stock for sku=" + sku + " warehouse=" + warehouse
            + " requested=" + requested + " available=" + available);
        this.sku = sku;
        this.warehouse = warehouse;
        this.requested = requested;
        this.available = available;
    }
}
