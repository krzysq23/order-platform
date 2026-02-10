package pl.xsware.payments.application.event

import java.time.Instant
import java.util.UUID

data class PaymentAuthorizedEvent(
    val eventId: UUID,
    val eventType: String,
    val version: Int,
    val occurredAt: Instant,
    val data: Data
) {

    companion object {
        const val TYPE = "PaymentAuthorized"
        const val VERSION = 1
    }

    data class Data(
        val paymentId: UUID,
        val orderId: UUID,
        val provider: String
    )
}
