package pl.xsware.inventory.application.reservation;

public interface ReserveStockUseCase {

    ReserveStockResult handle(ReserveStockRequest request);
}
