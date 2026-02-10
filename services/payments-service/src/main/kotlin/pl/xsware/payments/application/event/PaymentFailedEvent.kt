package pl.xsware.payments.application.event

import java.time.Instant
import java.util.UUID

data class PaymentFailedEvent(
    val eventId: UUID,
    val eventType: String,
    val version: Int,
    val occurredAt: Instant,
    val data: Data
) {
    companion object {
        const val TYPE = "PaymentFailed"
        const val VERSION = 1
    }

    data class Data(
        val paymentId: UUID,
        val orderId: UUID,
        val reason: String? = null
    )
}
