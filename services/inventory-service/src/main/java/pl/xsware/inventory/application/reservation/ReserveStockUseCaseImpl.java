package pl.xsware.inventory.application.reservation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.xsware.inventory.application.event.ProcessedEventsPort;
import pl.xsware.inventory.application.outbox.OutboxPort;
import pl.xsware.inventory.application.stock.StockItemRepositoryPort;
import pl.xsware.inventory.domain.reservation.ReserveStockCommand;
import pl.xsware.inventory.domain.reservation.StockAllocator;
import pl.xsware.inventory.domain.stock.vo.Quantity;
import pl.xsware.inventory.domain.stock.vo.Sku;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReserveStockUseCaseImpl implements ReserveStockUseCase {

    private static final String WAREHOUSE = StockAllocator.MAIN_WAREHOUSE;
    private static final String AGGREGATE_TYPE = "StockReservation";

    private final StockItemRepositoryPort stockItemRepository;
    private final StockReservationRepositoryPort reservationRepository;
    private final ProcessedEventsPort processedEvents;
    private final OutboxPort outbox;
    private final StockAllocator allocator;

    @Override
    @Transactional
    public ReserveStockResult handle(ReserveStockRequest request) {

        // 0) Idempotency (consumer-level)
        if (processedEvents.exists(request.eventId())) {
            log.info("ReserveStock skipped - already processed. eventId={}, orderId={}",
                request.eventId(), request.orderId());
            return new ReserveStockResult.AlreadyProcessed(request.eventId());
        }

        // 1) Map to domain command
        var cmd = new ReserveStockCommand(
            request.orderId(),
            request.correlationId(),
            request.expiresAt(),
            request.items().stream()
                .map(i -> new ReserveStockCommand.Item(new Sku(i.sku()), new Quantity(i.quantity())))
                .toList()
        );

        // 2) Load & lock stock items for MAIN warehouse
        var skus = cmd.items().stream().map(ReserveStockCommand.Item::sku).toList();
        var lockedStockItems = stockItemRepository.findForUpdateBySkusAndWarehouse(skus, WAREHOUSE);

        // 3) Domain allocation + get DomainEvents
        UUID reservationId = UUID.randomUUID();
        var allocation = allocator.allocate(cmd, reservationId, lockedStockItems);

        var domainEvents = allocation.reservation().pullDomainEvents();

        // 4) Persist changes
        var reservation = reservationRepository.save(allocation.reservation());
        stockItemRepository.saveAll(allocation.changedStockItems());

        // 5) Mark event processed (idempotency)
        processedEvents.markProcessed(
            request.eventId(),
            request.eventType(),
            request.orderId(),
            request.occurredAt()
        );

        // 6) Enqueue domain events to outbox;
        outbox.enqueueAll(domainEvents, AGGREGATE_TYPE, reservation.getId().toString());

        // 7) Return result + log
        if (reservation.getStatus().name().equals("RESERVED")) {
            log.info("Stock reserved. orderId={}, reservationId={}, warehouse={}",
                reservation.getOrderId(), reservation.getId(), WAREHOUSE);
            return new ReserveStockResult.Reserved(reservation.getOrderId(), reservation.getId());
        }

        log.warn("Stock reservation failed. orderId={}, reservationId={}, reason={}",
            reservation.getOrderId(), reservation.getId(), reservation.getReason());
        return new ReserveStockResult.Failed(reservation.getOrderId(), reservation.getId(), reservation.getReason());
    }
}
