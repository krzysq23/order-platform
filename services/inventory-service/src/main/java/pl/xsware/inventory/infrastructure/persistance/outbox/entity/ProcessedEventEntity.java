package pl.xsware.inventory.infrastructure.persistance.outbox.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "processed_events",
    uniqueConstraints = @UniqueConstraint(name = "uq_processed_events_event_id", columnNames = "event_id"),
    indexes = {
        @Index(name = "idx_processed_events_type_time", columnList = "event_type, processed_at DESC")
    })
public class ProcessedEventEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    @Column(name = "event_type", nullable = false, length = 128)
    private String eventType;

    @Column(name = "occurred_at")
    private Instant occurredAt;

    @Column(name = "aggregate_id")
    private UUID aggregateId;

    @Column(name = "payload_hash", length = 128)
    private String payloadHash;

    @Column(name = "processed_at", nullable = false)
    private Instant processedAt;

    @PrePersist
    void prePersist() {
        if (id == null) id = UUID.randomUUID();
        if (processedAt == null) processedAt = Instant.now();
    }
}
