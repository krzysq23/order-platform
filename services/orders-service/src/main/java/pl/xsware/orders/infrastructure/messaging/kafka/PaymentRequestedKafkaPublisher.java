package pl.xsware.orders.infrastructure.messaging.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import pl.xsware.orders.application.event.PaymentRequestedEvent;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class PaymentRequestedKafkaPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.kafka.topics.payment-requested}")
    private String topic;

    public void publish(PaymentRequestedEvent event) {
        String key = event.getData().getOrderId().toString();

        String payload;
        try {
            payload = objectMapper.writeValueAsString(event);
        } catch (JacksonException e) {
            throw new IllegalStateException("Cannot serialize PaymentRequestedEvent", e);
        }

        ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, payload);

        record.headers().add(new RecordHeader("eventId", event.getEventId().toString().getBytes(StandardCharsets.UTF_8)));
        record.headers().add(new RecordHeader("eventType", event.getEventType().getBytes(StandardCharsets.UTF_8)));
        record.headers().add(new RecordHeader("eventVersion", String.valueOf(event.getVersion()).getBytes(StandardCharsets.UTF_8)));
        record.headers().add(new RecordHeader("occurredAt", event.getOccurredAt().toString().getBytes(StandardCharsets.UTF_8)));

        kafkaTemplate.send(record);
    }
}
