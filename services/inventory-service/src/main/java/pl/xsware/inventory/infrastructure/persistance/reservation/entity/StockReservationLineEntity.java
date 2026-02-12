package pl.xsware.inventory.infrastructure.persistance.reservation.entity;

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
@Table(name = "stock_reservation_lines",
    indexes = {
        @Index(name = "idx_stock_res_lines_reservation", columnList = "reservation_id"),
        @Index(name = "idx_stock_res_lines_product", columnList = "product_id"),
        @Index(name = "idx_stock_res_lines_product_wh", columnList = "product_id, warehouse_code")
    })
public class StockReservationLineEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reservation_id", nullable = false,
        foreignKey = @ForeignKey(name = "fk_stock_res_lines_reservation"))
    private StockReservationEntity reservation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false,
        foreignKey = @ForeignKey(name = "fk_stock_res_lines_product"))
    private ProductEntity product;

    @Column(name = "warehouse_code", nullable = false, length = 32)
    private String warehouseCode;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        if (id == null) id = UUID.randomUUID();
        createdAt = Instant.now();
    }
}
