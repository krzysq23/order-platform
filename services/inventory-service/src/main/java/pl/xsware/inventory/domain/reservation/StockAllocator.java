package pl.xsware.inventory.domain.reservation;

import org.springframework.stereotype.Component;
import pl.xsware.inventory.domain.stock.NotEnoughStockException;
import pl.xsware.inventory.domain.stock.StockItem;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class StockAllocator {

    public static final String MAIN_WAREHOUSE = "MAIN";

    public AllocationResult allocate(ReserveStockCommand cmd, UUID reservationId, List<StockItem> stockItemsForMainWarehouse) {

        Objects.requireNonNull(cmd);
        Objects.requireNonNull(reservationId);
        Objects.requireNonNull(stockItemsForMainWarehouse);

        var bySku = stockItemsForMainWarehouse.stream()
            .filter(si -> MAIN_WAREHOUSE.equals(si.getWarehouse()))
            .collect(Collectors.toMap(si -> si.getSku().value(), si -> si, (a, b) -> a));

        var reservation = StockReservation.start(cmd, reservationId);

        try {

            for (var item : cmd.items()) {
                var sku = item.sku().value();
                var stockItem = bySku.get(sku);
                if (stockItem == null) {
                    reservation.markFailed("Stock item not found for sku=" + sku + " warehouse=" + MAIN_WAREHOUSE);
                    return AllocationResult.failed(reservation, List.of());
                }

                stockItem.reserve(item.quantity());
            }

            reservation.markReserved(MAIN_WAREHOUSE);
            return AllocationResult.reserved(reservation, stockItemsForMainWarehouse);

        } catch (NotEnoughStockException e) {
            reservation.markFailed(e.getMessage());
            return AllocationResult.failed(reservation, stockItemsForMainWarehouse);
        }
    }

    public sealed interface AllocationResult permits AllocationResult.Reserved, AllocationResult.Failed {
        StockReservation reservation();
        List<StockItem> changedStockItems();

        static Reserved reserved(StockReservation r, List<StockItem> items) { return new Reserved(r, items); }
        static Failed failed(StockReservation r, List<StockItem> items) { return new Failed(r, items); }

        record Reserved(StockReservation reservation, List<StockItem> changedStockItems) implements AllocationResult {}
        record Failed(StockReservation reservation, List<StockItem> changedStockItems) implements AllocationResult {}
    }
}
