package pl.xsware.orders.infrastructure.messaging.kafka;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import pl.xsware.orders.application.event.PaymentRequestedEvent;
import pl.xsware.orders.application.event.StockReservedEvent;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.exc.JsonNodeException;

import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class PaymentRequestedKafkaPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.kafka.topics.payment-requested}")
    private String topic;

    public void publish(PaymentRequestedEvent event) {
        String key = event.data().orderId().toString();

        String payload;
        try {
            payload = objectMapper.writeValueAsString(event);
        } catch (JsonNodeException e) {
            throw new IllegalStateException("Cannot serialize PaymentRequestedEvent", e);
        }


        ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, payload);

        record.headers().add(new RecordHeader("eventId", event.eventId().toString().getBytes(StandardCharsets.UTF_8)));
        record.headers().add(new RecordHeader("eventType", event.eventType().getBytes(StandardCharsets.UTF_8)));
        record.headers().add(new RecordHeader("eventVersion", String.valueOf(event.version()).getBytes(StandardCharsets.UTF_8)));
        record.headers().add(new RecordHeader("occurredAt", event.occurredAt().toString().getBytes(StandardCharsets.UTF_8)));

        kafkaTemplate.send(record);
    }

    public void publish(StockReservedEvent event) {
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
