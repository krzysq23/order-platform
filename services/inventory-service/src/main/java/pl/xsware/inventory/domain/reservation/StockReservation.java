package pl.xsware.inventory.domain.reservation;

import lombok.Getter;
import pl.xsware.inventory.domain.shared.DomainEvent;
import pl.xsware.inventory.domain.shared.StockReservationFailedDomainEvent;
import pl.xsware.inventory.domain.shared.StockReservedDomainEvent;
import pl.xsware.inventory.domain.stock.ReservationStateException;
import pl.xsware.inventory.domain.stock.vo.Quantity;
import pl.xsware.inventory.domain.stock.vo.Sku;

import java.time.Instant;
import java.util.*;

import static pl.xsware.inventory.domain.reservation.StockReservationStatus.*;

@Getter
public class StockReservation {

    private final UUID id;
    private final UUID orderId;

    private StockReservationStatus status;
    private UUID correlationId;
    private Instant expiresAt;
    private String reason;

    private final List<Line> lines = new ArrayList<>();
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    public StockReservation(UUID id, UUID orderId) {
        this.id = Objects.requireNonNull(id);
        this.orderId = Objects.requireNonNull(orderId);
        this.status = REQUESTED;
    }

    public static StockReservation start(ReserveStockCommand cmd, UUID reservationId) {
        var r = new StockReservation(reservationId, cmd.orderId());
        r.correlationId = cmd.correlationId();
        r.expiresAt = cmd.expiresAt();
        cmd.items().forEach(i -> r.addLine(i.sku(), i.quantity()));
        return r;
    }

    public void addLine(Sku sku, Quantity qty) {
        if (status != REQUESTED) throw new ReservationStateException("Cannot add lines in status " + status);
        lines.add(new Line(sku, qty));
    }

    public void markReserved(String warehouse) {
        requireStatus(REQUESTED);
        status = RESERVED;

        domainEvents.add(new StockReservedDomainEvent(
            UUID.randomUUID(),
            Instant.now(),
            orderId,
            id,
            lines.stream()
                .map(l -> new StockReservedDomainEvent.Line(l.sku.value(), warehouse, l.quantity.value()))
                .toList()
        ));
    }

    public void markFailed(String reason) {
        requireStatus(REQUESTED);
        status = FAILED;
        this.reason = reason;

        domainEvents.add(new StockReservationFailedDomainEvent(
            UUID.randomUUID(),
            Instant.now(),
            orderId,
            id,
            reason
        ));
    }

    private void requireStatus(StockReservationStatus expected) {
        if (status != expected) {
            throw new ReservationStateException("Expected status " + expected + " but was " + status);
        }
    }

    public List<DomainEvent> pullDomainEvents() {
        var copy = List.copyOf(domainEvents);
        domainEvents.clear();
        return copy;
    }

    public record Line(Sku sku, Quantity quantity) {}
}
