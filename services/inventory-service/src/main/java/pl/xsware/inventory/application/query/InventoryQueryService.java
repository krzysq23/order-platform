package pl.xsware.inventory.application.query;

import pl.xsware.inventory.infrastructure.web.dto.ProductStockDto;
import pl.xsware.inventory.infrastructure.web.dto.StockReservationDto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InventoryQueryService {

    List<ProductStockDto> findProductsByMinAvailable(int minAvailable, String warehouse, int limit);

    Optional<StockReservationDto> findReservation(UUID reservationId);

    List<StockReservationDto> findReservationsByOrderId(UUID orderId, int limit);
}
