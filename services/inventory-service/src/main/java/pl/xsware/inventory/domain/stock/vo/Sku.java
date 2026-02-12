package pl.xsware.inventory.domain.stock.vo;

import java.util.Objects;

public record Sku(String value) {

    public Sku {
        Objects.requireNonNull(value, "sku");
        var v = value.trim();
        if (v.isEmpty() || v.length() > 64) throw new IllegalArgumentException("Invalid sku");
        value = v;
    }
}
