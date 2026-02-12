package pl.xsware.inventory.infrastructure.persistance.reservation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.xsware.inventory.infrastructure.persistance.reservation.entity.StockReservationLineEntity;

import java.util.List;
import java.util.UUID;

public interface StockReservationLineJpaRepository extends JpaRepository<StockReservationLineEntity, UUID> {

    List<StockReservationLineEntity> findAllByReservation_Id(UUID reservationId);

    List<StockReservationLineEntity> findAllByProduct_Id(UUID productId);

    void deleteAllByReservation_Id(UUID reservationId);
}
