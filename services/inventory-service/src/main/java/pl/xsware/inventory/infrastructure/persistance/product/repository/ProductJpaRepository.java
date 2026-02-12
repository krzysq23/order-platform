package pl.xsware.inventory.infrastructure.persistance.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.xsware.inventory.infrastructure.persistance.product.entity.ProductEntity;

import java.util.Optional;
import java.util.UUID;

public interface ProductJpaRepository extends JpaRepository<ProductEntity, UUID> {

    Optional<ProductEntity> findBySku(String sku);

    boolean existsBySku(String sku);
}
