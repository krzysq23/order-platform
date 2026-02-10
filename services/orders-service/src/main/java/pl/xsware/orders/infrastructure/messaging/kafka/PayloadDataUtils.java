package pl.xsware.orders.infrastructure.messaging.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.exc.JsonNodeException;

@Slf4j
@Component
@RequiredArgsConstructor
public class PayloadDataUtils {

    private final ObjectMapper objectMapper;

    /**
     * Deserialize Kafka payload to target type.
     *
     * @param payload   raw message payload
     * @param type      target class
     * @param topicName Kafka topic (for diagnostics)
     * @param <T>       target type
     * @return deserialized object
     */
    public <T> T deserialize(String payload, Class<T> type, String topicName) {
        try {
            return objectMapper.readValue(payload, type);
        } catch (JsonNodeException e) {
            log.error(
                "Cannot deserialize payload from topic='{}' to type='{}'. Payload={}",
                topicName,
                type.getSimpleName(),
                payload,
                e
            );
            throw new PayloadDeserializationException(topicName, type, payload, e);
        }
    }
}
