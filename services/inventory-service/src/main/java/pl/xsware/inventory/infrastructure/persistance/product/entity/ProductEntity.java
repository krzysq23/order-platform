package pl.xsware.inventory.infrastructure.persistance.product.entity;

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
@Table(name = "products",
    uniqueConstraints = @UniqueConstraint(name = "uq_products_sku", columnNames = "sku"),
    indexes = {
        @Index(name = "idx_products_active", columnList = "is_active")
    })
public class ProductEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "sku", nullable = false, length = 64)
    private String sku;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

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
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }
}
