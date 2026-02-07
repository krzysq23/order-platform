package pl.xsware.payments.infrastucture.web.dto

import pl.xsware.payments.application.dto.PaymentResult
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class PaymentDto(
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
        fun from(r: PaymentResult) = PaymentDto(
            id = r.id,
            orderId = r.orderId,
            status = r.status,
            amount = r.amount,
            currency = r.currency,
            provider = r.provider,
            externalId = r.externalId,
            createdAt = r.createdAt,
            updatedAt = r.updatedAt
        )
    }
}
