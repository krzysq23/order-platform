package pl.xsware.inventory.infrastructure.messaging.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import pl.xsware.inventory.infrastructure.persistance.outbox.entity.OutboxMessageEntity;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryKafkaPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final InventoryTopicResolver topicResolver;

    public void publish(OutboxMessageEntity msg) {
        try {
            String topic = topicResolver.resolve(msg.getEventType(), msg.getVersion());
            String key = msg.getKey() != null ? msg.getKey() : msg.getAggregateId().toString();
            String payload = objectMapper.writeValueAsString(msg.getPayload());

            kafkaTemplate.send(topic, key, payload).get();

            log.debug("Outbox published. eventId={}, topic={}, key={}",
                msg.getEventId(), topic, key);

        } catch (Exception ex) {
            throw new RuntimeException("Kafka publish failed for eventId=" + msg.getEventId(), ex);
        }
    }
}
