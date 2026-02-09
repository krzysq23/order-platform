package pl.xsware.orders.infrastructure.persistence.saga;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SagaInstanceRepository extends JpaRepository<SagaInstanceEntity, UUID> {

    Optional<SagaInstanceEntity> findBySagaTypeAndAggregateId(String sagaType, String aggregateId);
}
