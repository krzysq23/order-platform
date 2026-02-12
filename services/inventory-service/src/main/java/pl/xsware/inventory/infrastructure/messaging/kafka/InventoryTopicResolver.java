package pl.xsware.inventory.infrastructure.messaging.kafka;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pl.xsware.inventory.domain.shared.StockReservationFailedDomainEvent;
import pl.xsware.inventory.domain.shared.StockReservedDomainEvent;

@Component
public class InventoryTopicResolver {

    @Value("${app.kafka.topics.stock-reserved}")
    private String stockReservedTopic;

    @Value("${app.kafka.topics.stock-reservation-failed}")
    private String stockReservationFailedTopic;

    public String resolve(String eventType, int version) {
        return switch (eventType) {
            case StockReservedDomainEvent.TYPE -> stockReservedTopic;
            case StockReservationFailedDomainEvent.TYPE -> stockReservationFailedTopic;
            default -> throw new IllegalArgumentException(
                "No topic mapping for eventType=" + eventType + " v=" + version
            );
        };
    }
}
