package pl.xsware.inventory.infrastructure.persistance.outbox.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.xsware.inventory.infrastructure.persistance.outbox.entity.ProcessedEventEntity;

import java.util.Optional;
import java.util.UUID;

public interface ProcessedEventJpaRepository extends JpaRepository<ProcessedEventEntity, UUID> {

    boolean existsByEventId(UUID eventId);

    Optional<ProcessedEventEntity> findByEventId(UUID eventId);
}
