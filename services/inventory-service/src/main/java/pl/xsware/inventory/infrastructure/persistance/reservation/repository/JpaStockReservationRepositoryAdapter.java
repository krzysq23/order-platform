package pl.xsware.inventory.infrastructure.persistance.reservation.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import pl.xsware.inventory.application.reservation.StockReservationRepositoryPort;
import pl.xsware.inventory.domain.reservation.StockReservation;
import pl.xsware.inventory.domain.reservation.StockReservationStatus;
import pl.xsware.inventory.domain.stock.vo.Quantity;
import pl.xsware.inventory.domain.stock.vo.Sku;
import pl.xsware.inventory.infrastructure.persistance.product.repository.ProductJpaRepository;
import pl.xsware.inventory.infrastructure.persistance.reservation.entity.StockReservationEntity;
import pl.xsware.inventory.infrastructure.persistance.reservation.entity.StockReservationLineEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Repository
@RequiredArgsConstructor
public class JpaStockReservationRepositoryAdapter
    implements StockReservationRepositoryPort {

    private static final String WAREHOUSE = "MAIN";

    private final StockReservationJpaRepository reservationRepo;
    private final ProductJpaRepository productRepo;

    @Override
    public Optional<StockReservation> findByOrderId(UUID orderId) {
        return reservationRepo.findByOrderId(orderId).map(this::toDomain);
    }

    @Override
    public StockReservation save(StockReservation reservation) {
        var entity = toEntity(reservation);
        var saved = reservationRepo.save(entity);
        return toDomain(saved);
    }

    private StockReservationEntity toEntity(StockReservation domain) {
        var e = new StockReservationEntity();

        e.setId(domain.getId());
        e.setOrderId(domain.getOrderId());
        e.setStatus(StockReservationStatus
            .valueOf(domain.getStatus().name()));
        e.setCorrelationId(domain.getCorrelationId());
        e.setExpiresAt(domain.getExpiresAt());
        e.setReason(domain.getReason());

        e.getLines().clear();
        for (var line : domain.getLines()) {
            var product = productRepo.findBySku(line.sku().value())
                .orElseThrow(() ->
                    new IllegalStateException("Product not found for sku=" + line.sku().value()));

            var le = new StockReservationLineEntity();
            le.setProduct(product);
            le.setWarehouseCode(WAREHOUSE);
            le.setQuantity(line.quantity().value());

            e.addLine(le);
        }

        return e;
    }

    private StockReservation toDomain(StockReservationEntity e) {
        List<StockReservation.Line> lines =
            e.getLines().stream()
                .map(le -> new StockReservation.Line(
                    new Sku(le.getProduct().getSku()),
                    new Quantity(le.getQuantity())
                ))
                .toList();

        return StockReservation.rehydrate(
            e.getId(),
            e.getOrderId(),
            StockReservationStatus.valueOf(e.getStatus().name()),
            e.getCorrelationId(),
            e.getExpiresAt(),
            e.getReason(),
            lines
        );
    }
}
