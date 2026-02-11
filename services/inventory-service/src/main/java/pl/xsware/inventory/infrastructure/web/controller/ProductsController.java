package pl.xsware.inventory.infrastructure.web.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import pl.xsware.inventory.application.query.InventoryQueryService;
import pl.xsware.inventory.infrastructure.web.dto.ProductStockDto;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/inventory")
public class ProductsController {

    private final InventoryQueryService queryService;

    @GetMapping("/products")
    public List<ProductStockDto> getProductsByMinAvailable(
        @RequestParam(defaultValue = "1") int minAvailable,
        @RequestParam(defaultValue = "MAIN") String warehouse,
        @RequestParam(defaultValue = "100") int limit
    ) {
        log.info("GET /inventory/products minAvailable={} warehouse={} limit={}",
            minAvailable, warehouse, limit);

        return queryService.findProductsByMinAvailable(minAvailable, warehouse, limit);
    }
}
