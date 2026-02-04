package pl.xsware.orders.infrastructure.persistence.order;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OrderJpaRepository extends JpaRepository<OrderJpaEntity, UUID> {

    List<OrderJpaEntity> findAllByCustomerId(String customerId);
}
