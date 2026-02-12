package pl.xsware.inventory.infrastructure.persistance.reservation.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Query("""
        select r
        from StockReservationEntity r
        left join fetch r.lines l
        where r.id = :id
    """)
    Optional<StockReservationEntity> findByIdWithLines(@Param("id") UUID id);

    @Query("""
        select distinct r
        from StockReservationEntity r
        left join fetch r.lines l
        where r.orderId = :orderId
        order by r.createdAt desc
    """)
    List<StockReservationEntity> findByOrderIdWithLines(
        @Param("orderId") UUID orderId,
        Pageable pageable
    );
}
