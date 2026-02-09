package pl.xsware.payments.infrastucture.persistence.payment.entity

import jakarta.persistence.*
import pl.xsware.payments.domain.model.PaymentStatus
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "payments")
class PaymentEntity(

    @Id
    var id: UUID,

    @Column(name = "order_id", nullable = false)
    var orderId: UUID,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    var status: PaymentStatus,

    @Column(nullable = false, precision = 19, scale = 2)
    var amount: BigDecimal,

    @Column(nullable = false, length = 8)
    var currency: String,

    @Column(nullable = true, length = 64)
    var provider: String? = null,

    @Column(name = "external_id", nullable = true, length = 128)
    var externalId: String? = null,

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant,

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant
)
