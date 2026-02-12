package pl.xsware.inventory.infrastructure.persistance.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.xsware.inventory.infrastructure.persistance.product.entity.ProductCategoryEntity;

public interface ProductCategoryJpaRepository extends JpaRepository<ProductCategoryEntity, String> {
}
