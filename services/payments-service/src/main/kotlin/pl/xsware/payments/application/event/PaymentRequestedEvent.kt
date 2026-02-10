package pl.xsware.payments.application.event

import java.time.Instant
import java.util.UUID

data class PaymentRequestedEvent(
    val eventId: UUID,
    val eventType: String,
    val version: Int,
    val occurredAt: Instant,
    val data: Data
) {

    companion object {
        const val TYPE = "PaymentRequested"
        const val VERSION = 1
    }

    data class Data(
        val orderId: UUID,
        val amount: String,
        val currency: String
    )
}
