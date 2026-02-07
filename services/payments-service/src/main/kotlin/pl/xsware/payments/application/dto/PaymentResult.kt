package pl.xsware.payments.application.dto

import pl.xsware.payments.domain.model.Payment
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class PaymentResult(
    val id: UUID,
    val orderId: UUID,
    val status: String,
    val amount: BigDecimal,
    val currency: String,
    val provider: String?,
    val externalId: String?,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    companion object {
        fun from(p: Payment) = PaymentResult(
            id = p.id,
            orderId = p.orderId,
            status = p.status.name,
            amount = p.amount,
            currency = p.currency,
            provider = p.provider,
            externalId = p.externalId,
            createdAt = p.createdAt,
            updatedAt = p.updatedAt
        )
    }
}
