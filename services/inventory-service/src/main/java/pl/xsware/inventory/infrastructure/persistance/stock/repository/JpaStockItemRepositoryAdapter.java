package pl.xsware.inventory.infrastructure.persistance.stock.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import pl.xsware.inventory.application.stock.StockItemRepositoryPort;
import pl.xsware.inventory.domain.stock.StockItem;
import pl.xsware.inventory.domain.stock.vo.Sku;
import pl.xsware.inventory.infrastructure.persistance.product.entity.ProductEntity;
import pl.xsware.inventory.infrastructure.persistance.product.repository.ProductJpaRepository;
import pl.xsware.inventory.infrastructure.persistance.stock.entity.StockItemEntity;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Repository
@RequiredArgsConstructor
public class JpaStockItemRepositoryAdapter implements StockItemRepositoryPort {

    private final ProductJpaRepository productRepo;
    private final StockItemJpaRepository stockItemRepo;

    @Override
    public List<StockItem> findForUpdateBySkusAndWarehouse(List<Sku> skus, String warehouse) {

        if (skus == null || skus.isEmpty()) return List.of();

        // Resolve products by SKU (N+1 for MVP; OK). If you want, we can optimize with IN-query later.
        List<ProductEntity> products = skus.stream()
            .map(Sku::value)
            .map(sku -> productRepo.findBySku(sku).orElse(null))
            .filter(Objects::nonNull)
            .toList();

        var foundSkus = products.stream().map(ProductEntity::getSku).collect(Collectors.toSet());
        var missingSkus = skus.stream().map(Sku::value).filter(s -> !foundSkus.contains(s)).toList();
        if (!missingSkus.isEmpty()) {
            log.warn("Products not found for SKUs: {}", missingSkus);
        }

        // Lock stock items per product in MAIN warehouse
        List<StockItem> result = new ArrayList<>();
        for (var p : products) {
            Optional<StockItemEntity> locked = stockItemRepo.findForUpdate(p.getId(), warehouse);
            if (locked.isEmpty()) {
                log.warn("StockItem not found for sku={} warehouse={}", p.getSku(), warehouse);
                continue;
            }
            result.add(toDomain(locked.get(), p));
        }

        return result;
    }

    @Override
    public List<StockItem> saveAll(List<StockItem> items) {

        if (items == null || items.isEmpty()) return List.of();

        // We update existing entities (by id). Stock items should exist.
        List<UUID> ids = items.stream().map(StockItem::getId).toList();
        Map<UUID, StockItemEntity> entities = stockItemRepo.findAllById(ids).stream()
            .collect(Collectors.toMap(StockItemEntity::getId, e -> e));

        for (var domain : items) {
            var entity = entities.get(domain.getId());
            if (entity == null) {
                log.error("StockItemEntity missing while saving. id={}, sku={}, warehouse={}",
                    domain.getId(), domain.getSku().value(), domain.getWarehouse());
                continue;
            }

            // Update mutable state
            entity.setQuantityOnHand(domain.getOnHand());
            entity.setQuantityReserved(domain.getReserved());
        }

        stockItemRepo.saveAll(entities.values());

        // Return refreshed domain objects (best-effort)
        return entities.values().stream()
            .map(e -> toDomain(e, e.getProduct()))
            .toList();
    }

    private StockItem toDomain(StockItemEntity entity, ProductEntity product) {
        return new StockItem(
            entity.getId(),
            new Sku(product.getSku()),
            entity.getWarehouseCode(),
            entity.getQuantityOnHand(),
            entity.getQuantityReserved()
        );
    }
}
