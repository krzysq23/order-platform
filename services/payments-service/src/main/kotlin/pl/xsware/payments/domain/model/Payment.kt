package pl.xsware.payments.domain.model

import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

class Payment (
    val id: UUID,
    val orderId: UUID,
    var status: PaymentStatus,
    val amount: BigDecimal,
    val currency: String,
    val provider: String?,
    val externalId: String?,
    val createdAt: Instant,
    var updatedAt: Instant
) {
    companion object {

        fun createRequested(
            orderId: UUID,
            amount: BigDecimal,
            currency: String,
            provider: String? = null,
            externalId: String? = null
        ): Payment {

            require(currency.isNotBlank()) { "currency cannot be blank" }
            require(amount > BigDecimal.ZERO) { "amount must be > 0" }

            val now = Instant.now()
            return Payment(
                id = UUID.randomUUID(),
                orderId = orderId,
                status = PaymentStatus.REQUESTED,
                amount = amount,
                currency = currency,
                provider = provider,
                externalId = externalId,
                createdAt = now,
                updatedAt = now
            )
        }
    }

    fun markAuthorized() {
        require(status == PaymentStatus.REQUESTED) { "Cannot authorize payment in status=$status" }
        status = PaymentStatus.AUTHORIZED
        updatedAt = Instant.now()
    }

    fun markFailed() {
        if (status == PaymentStatus.CAPTURED) error("Cannot fail captured payment")
        status = PaymentStatus.FAILED
        updatedAt = Instant.now()
    }
}
