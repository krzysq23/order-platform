package pl.xsware.inventory.infrastructure.messaging.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import pl.xsware.inventory.application.event.ReserveStockRequestFactory;
import pl.xsware.inventory.application.event.ReserveStockRequestedEvent;
import pl.xsware.inventory.application.reservation.ReserveStockUseCase;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReserveStockKafkaListener {

    private final ObjectMapper objectMapper;
    private final ReserveStockUseCase reserveStockUseCase;

    @KafkaListener(
        topics = "${app.kafka.topics.reserve-stock-requested}",
        groupId = "${spring.kafka.consumer.group-id}"
    )
    public void onMessage(ConsumerRecord<String, String> record) {

        String payload = record.value();

        try {
            ReserveStockRequestedEvent event =
                objectMapper.readValue(payload, ReserveStockRequestedEvent.class);

            if (!ReserveStockRequestedEvent.TYPE.equals(event.eventType())
                || event.version() != ReserveStockRequestedEvent.VERSION) {
                log.warn("Unsupported ReserveStockRequested event. type={}, version={}, offset={}",
                    event.eventType(), event.version(), record.offset());
                return;
            }

            log.info("ReserveStockRequested received. eventId={}, orderId={}, offset={}",
                event.eventId(), event.data().orderId(), record.offset());

            var request = ReserveStockRequestFactory.create(event);
            reserveStockUseCase.handle(request);

        } catch (Exception ex) {
            log.error("Failed to process ReserveStockRequested. topic={}, partition={}, offset={}",
                record.topic(), record.partition(), record.offset(), ex);
            throw new RuntimeException(ex);
        }
    }
}
