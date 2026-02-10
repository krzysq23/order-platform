package pl.xsware.inventory.application.reservation;

import pl.xsware.inventory.domain.reservation.StockReservation;

import java.util.Optional;
import java.util.UUID;

public interface StockReservationRepositoryPort {

    Optional<StockReservation> findByOrderId(UUID orderId);

    StockReservation save(StockReservation reservation);
}
