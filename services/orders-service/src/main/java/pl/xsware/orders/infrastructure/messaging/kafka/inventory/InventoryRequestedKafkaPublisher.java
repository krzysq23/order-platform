package pl.xsware.orders.infrastructure.messaging.kafka.inventory;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import pl.xsware.orders.application.event.ReserveStockRequestedEvent;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.exc.JsonNodeException;

import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class InventoryRequestedKafkaPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.kafka.topics.reserve-stock-requested}")
    private String topic;

    public void publish(ReserveStockRequestedEvent event) {
        String key = event.data().orderId().toString();

        String payload;
        try {
            payload = objectMapper.writeValueAsString(event);
        } catch (JsonNodeException e) {
            throw new IllegalStateException("Cannot serialize StockReservedEvent", e);
        }


        ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, payload);

        record.headers().add(new RecordHeader("eventId", event.eventId().toString().getBytes(StandardCharsets.UTF_8)));
        record.headers().add(new RecordHeader("eventType", event.eventType().getBytes(StandardCharsets.UTF_8)));
        record.headers().add(new RecordHeader("eventVersion", String.valueOf(event.version()).getBytes(StandardCharsets.UTF_8)));
        record.headers().add(new RecordHeader("occurredAt", event.occurredAt().toString().getBytes(StandardCharsets.UTF_8)));

        kafkaTemplate.send(record);
    }

}
