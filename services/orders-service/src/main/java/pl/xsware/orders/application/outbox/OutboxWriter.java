package pl.xsware.orders.application.outbox;

import pl.xsware.orders.domain.shared.DomainEvent;
import pl.xsware.orders.domain.shared.OutboxEvent;

import java.util.List;

public interface OutboxWriter {

    void write(OutboxEvent event);

    default void writeAll(List<? extends OutboxEvent> events) {
        for (OutboxEvent event : events) {
            write(event);
        }
    }
}
