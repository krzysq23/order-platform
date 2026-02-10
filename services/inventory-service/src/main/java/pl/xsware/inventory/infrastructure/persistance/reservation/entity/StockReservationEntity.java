package pl.xsware.inventory.infrastructure.persistance.reservation.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import pl.xsware.inventory.domain.reservation.StockReservationStatus;

import java.time.Instant;
import java.util.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "stock_reservations",
    uniqueConstraints = @UniqueConstraint(name = "uq_stock_reservations_order_id", columnNames = "order_id"),
    indexes = {
        @Index(name = "idx_stock_reservations_status", columnList = "status"),
        @Index(name = "idx_stock_reservations_expires_at", columnList = "expires_at")
    })
public class StockReservationEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private StockReservationStatus status;

    @Column(name = "correlation_id")
    private UUID correlationId;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "reason", length = 512)
    private String reason;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "data", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> data = new HashMap<>();

    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StockReservationLineEntity> lines = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        if (id == null) id = UUID.randomUUID();
        var now = Instant.now();
        createdAt = now;
        updatedAt = now;
        if (status == null) status = StockReservationStatus.REQUESTED;
        if (data == null) data = new HashMap<>();
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }

    public void addLine(StockReservationLineEntity line) {
        line.setReservation(this);
        lines.add(line);
    }
}
