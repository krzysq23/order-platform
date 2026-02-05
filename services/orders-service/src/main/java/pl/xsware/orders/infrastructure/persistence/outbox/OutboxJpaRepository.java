package pl.xsware.orders.infrastructure.persistence.outbox;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OutboxJpaRepository extends JpaRepository<OutboxMessageEntity, UUID> {

    List<OutboxMessageEntity> findByProcessedAtIsNullOrderByCreatedAtDesc(Pageable pageable);
}
