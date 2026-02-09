package pl.xsware.payments.application.event

import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class PaymentSucceededEvent(
    val eventId: UUID,
    val eventType: String,
    val version: Int,
    val occurredAt: Instant,
    val data: Data
) {
    companion object {
        const val TYPE = "PaymentSucceeded"
        const val VERSION = 1
    }

    data class Data(
        val paymentId: UUID,
        val orderId: UUID,
        val amount: BigDecimal,
        val currency: String,
        val provider: String,
        val externalId: String
    )
}
