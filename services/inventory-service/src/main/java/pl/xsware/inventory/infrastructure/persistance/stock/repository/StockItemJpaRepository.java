package pl.xsware.inventory.infrastructure.persistance.stock.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import pl.xsware.inventory.infrastructure.persistance.stock.entity.StockItemEntity;
import pl.xsware.inventory.infrastructure.persistance.view.ProductStockView;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StockItemJpaRepository extends JpaRepository<StockItemEntity, UUID> {

    List<StockItemEntity> findAllByProduct_Id(UUID productId);

    Optional<StockItemEntity> findByProduct_IdAndWarehouseCode(UUID productId, String warehouseCode);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select si
        from StockItemEntity si
        where si.product.id = :productId
          and si.warehouseCode = :warehouseCode
        """)
    Optional<StockItemEntity> findForUpdate(
        @Param("productId") UUID productId,
        @Param("warehouseCode") String warehouseCode
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select si
        from StockItemEntity si
        where si.product.id = :productId
        """)
    List<StockItemEntity> findAllForUpdateByProductId(@Param("productId") UUID productId);

    @Query("""
        select
            p.id as productId,
            p.sku as sku,
            p.name as name,
            c.code as categoryCode,
            si.quantityOnHand as onHand,
            si.quantityReserved as reserved,
            (si.quantityOnHand - si.quantityReserved) as available
        from StockItemEntity si
        join si.product p
        join p.category c
        where si.warehouseCode = :warehouse
          and (si.quantityOnHand - si.quantityReserved) >= :minAvailable
        order by (si.quantityOnHand - si.quantityReserved) desc, p.sku asc
    """)
    List<ProductStockView> findByMinAvailable(
        @Param("minAvailable") int minAvailable,
        @Param("warehouse") String warehouse,
        Pageable pageable
    );
}
