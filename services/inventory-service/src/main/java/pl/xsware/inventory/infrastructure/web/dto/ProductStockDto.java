package pl.xsware.inventory.infrastructure.web.dto;

import java.util.UUID;

public record ProductStockDto(
    UUID productId,
    String sku,
    String name,
    String categoryCode,
    int onHand,
    int reserved,
    int available
) {}
