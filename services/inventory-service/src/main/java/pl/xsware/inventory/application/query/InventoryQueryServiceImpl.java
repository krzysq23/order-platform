package pl.xsware.inventory.application.query;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.xsware.inventory.infrastructure.persistance.reservation.entity.StockReservationEntity;
import pl.xsware.inventory.infrastructure.persistance.reservation.repository.StockReservationJpaRepository;
import pl.xsware.inventory.infrastructure.persistance.stock.repository.StockItemJpaRepository;
import pl.xsware.inventory.infrastructure.persistance.view.ProductStockView;
import pl.xsware.inventory.infrastructure.web.dto.ProductStockDto;
import pl.xsware.inventory.infrastructure.web.dto.StockReservationDto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryQueryServiceImpl implements InventoryQueryService {

    private final StockItemJpaRepository stockItemRepo;
    private final StockReservationJpaRepository reservationRepo;

    @Override
    @Transactional(readOnly = true)
    public List<ProductStockDto> findProductsByMinAvailable(int minAvailable, String warehouse, int limit) {

        int safeLimit = Math.max(1, Math.min(limit, 500));
        String wh = (warehouse == null || warehouse.isBlank()) ? "MAIN" : warehouse;

        log.debug("Query products by minAvailable={}, warehouse={}, limit={}", minAvailable, wh, safeLimit);

        List<ProductStockView> rows = stockItemRepo.findByMinAvailable(
            minAvailable,
            wh,
            PageRequest.of(0, safeLimit)
        );

        return rows.stream()
            .map(r -> new ProductStockDto(
                r.getProductId(),
                r.getSku(),
                r.getName(),
                r.getCategoryCode(),
                r.getOnHand(),
                r.getReserved(),
                r.getAvailable()
            ))
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<StockReservationDto> findReservation(UUID reservationId) {

        return reservationRepo.findByIdWithLines(reservationId)
            .map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockReservationDto> findReservationsByOrderId(UUID orderId, int limit) {

        int safeLimit = Math.max(1, Math.min(limit, 200));

        return reservationRepo.findByOrderIdWithLines(orderId, PageRequest.of(0, safeLimit))
            .stream()
            .map(this::toDto)
            .toList();
    }

    private StockReservationDto toDto(StockReservationEntity r) {

        return new StockReservationDto(
            r.getId(),
            r.getOrderId(),
            "MAIN",
            r.getStatus().name(),
            r.getCreatedAt(),
            r.getExpiresAt(),
            r.getLines().stream()
                .map(l -> new StockReservationDto.Line(l.getProduct().getSku(), l.getQuantity()))
                .toList()
        );
    }
}
