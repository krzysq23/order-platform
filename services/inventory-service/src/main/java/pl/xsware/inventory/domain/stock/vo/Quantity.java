package pl.xsware.inventory.domain.stock.vo;

public record Quantity(int value) {

    public Quantity {
        if (value <= 0) throw new IllegalArgumentException("Quantity must be > 0");
    }
}
