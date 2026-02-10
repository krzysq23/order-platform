package pl.xsware.orders.infrastructure.persistence.saga;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import pl.xsware.orders.domain.saga.SagaState;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "saga_instances",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "ux_saga_type_aggregate",
            columnNames = {"saga_type", "aggregate_id"}
        )
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SagaInstanceEntity {

    @Id
    @Column(name = "saga_id", nullable = false)
    private UUID sagaId;

    @Column(name = "saga_type", nullable = false, length = 100)
    private String sagaType;

    @Column(name = "aggregate_id", nullable = false, length = 255)
    private String aggregateId;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false, length = 50)
    private SagaState state;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "data", nullable = false, columnDefinition = "jsonb")
    private JsonNode data;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public static SagaInstanceEntity start(UUID sagaId, String sagaType, String aggregateId, ObjectMapper om) {
        var now = Instant.now();
        var e = new SagaInstanceEntity();
        e.sagaId = sagaId;
        e.sagaType = sagaType;
        e.aggregateId = aggregateId;
        e.state = SagaState.PAYMENT_REQUESTED;
        e.data = om.createObjectNode();
        e.createdAt = now;
        e.updatedAt = now;
        return e;
    }

    public void transitionTo(SagaState newState) {
        this.state = newState;
        this.updatedAt = Instant.now();
    }

    public ObjectNode dataObject(ObjectMapper om) {
        if (this.data == null || !this.data.isObject()) {
            this.data = om.createObjectNode();
        }
        return (ObjectNode) this.data;
    }

    public void putString(String field, String value, ObjectMapper om) {
        if (value == null) return;
        dataObject(om).put(field, value);
        this.updatedAt = Instant.now();
    }

    public void putUuid(String field, UUID value, ObjectMapper om) {
        if (value == null) return;
        dataObject(om).put(field, value.toString());
        this.updatedAt = Instant.now();
    }
}
