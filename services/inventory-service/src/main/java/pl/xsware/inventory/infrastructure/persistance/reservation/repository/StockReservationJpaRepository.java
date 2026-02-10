package pl.xsware.inventory.infrastructure.persistance.reservation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.xsware.inventory.domain.reservation.StockReservationStatus;
import pl.xsware.inventory.infrastructure.persistance.reservation.entity.StockReservationEntity;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StockReservationJpaRepository extends JpaRepository<StockReservationEntity, UUID> {

    Optional<StockReservationEntity> findByOrderId(UUID orderId);

    List<StockReservationEntity> findAllByStatus(StockReservationStatus status);

    List<StockReservationEntity> findAllByStatusAndExpiresAtBefore(StockReservationStatus status, Instant now);

    List<StockReservationEntity> findAllByExpiresAtBeforeAndStatusIn(Instant now, List<StockReservationStatus> statuses);
}
