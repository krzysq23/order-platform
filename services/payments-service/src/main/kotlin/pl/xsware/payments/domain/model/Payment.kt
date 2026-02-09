package pl.xsware.payments.domain.model

import java.math.BigDecimal
import java.time.Clock
import java.time.Instant
import java.util.UUID

class Payment (
    val id: UUID,
    val orderId: UUID,
    var status: PaymentStatus,
    val amount: BigDecimal,
    val currency: String,
    var provider: String?,
    var externalId: String?,
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
            require(amount.scale() <= 2) { "amount scale must be <= 2" }

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

    fun markAuthorized(
        provider: String,
        externalId: String? = null,
        clock: Clock = Clock.systemUTC()
    ) {
        require(status == PaymentStatus.REQUESTED) {
            "Cannot authorize payment in status=$status"
        }
        require(provider.isNotBlank()) { "provider cannot be blank" }

        status = PaymentStatus.AUTHORIZED
        this.provider = provider
        if (externalId != null) this.externalId = externalId
        updatedAt = Instant.now(clock)
    }

    fun markCaptured(
        provider: String,
        externalId: String,
        clock: Clock = Clock.systemUTC()
    ) {
        require(status == PaymentStatus.AUTHORIZED) {
            "Cannot capture payment in status=$status"
        }
        require(provider.isNotBlank()) { "provider cannot be blank" }
        require(externalId.isNotBlank()) { "externalId cannot be blank" }
        status = PaymentStatus.CAPTURED
        this.provider = provider
        this.externalId = externalId
        updatedAt = Instant.now(clock)
    }

    fun markFailed(clock: Clock = Clock.systemUTC()) {
        require(status != PaymentStatus.CAPTURED) { "Cannot fail captured payment" }
        require(status != PaymentStatus.CANCELLED) { "Cannot fail cancelled payment" }
        status = PaymentStatus.FAILED
        updatedAt = Instant.now(clock)
    }

    fun markCancelled(clock: Clock = Clock.systemUTC()) {
        require(status != PaymentStatus.CAPTURED) { "Cannot cancel captured payment" }
        require(status != PaymentStatus.FAILED) { "Cannot cancel failed payment" }
        status = PaymentStatus.CANCELLED
        updatedAt = Instant.now(clock)
    }
}
