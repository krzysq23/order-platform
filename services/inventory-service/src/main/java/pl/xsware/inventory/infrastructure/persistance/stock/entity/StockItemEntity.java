package pl.xsware.inventory.infrastructure.persistance.stock.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.xsware.inventory.infrastructure.persistance.product.entity.ProductEntity;

import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "stock_items",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_stock_items_product_warehouse",
        columnNames = {"product_id", "warehouse_code"}
    ),
    indexes = {
        @Index(name = "idx_stock_items_product", columnList = "product_id"),
        @Index(name = "idx_stock_items_warehouse", columnList = "warehouse_code"),
        @Index(name = "idx_stock_items_available",
            columnList = "product_id, warehouse_code, quantity_on_hand, quantity_reserved")
    })
public class StockItemEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false,
        foreignKey = @ForeignKey(name = "fk_stock_items_product"))
    private ProductEntity product;

    @Column(name = "warehouse_code", nullable = false, length = 32)
    private String warehouseCode;

    @Column(name = "quantity_on_hand", nullable = false)
    private int quantityOnHand;

    @Column(name = "quantity_reserved", nullable = false)
    private int quantityReserved;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

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

    public int available() {
        return quantityOnHand - quantityReserved;
    }
}
