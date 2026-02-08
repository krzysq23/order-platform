package pl.xsware.payments.infrastucture.messaging.outbox

import org.springframework.stereotype.Component

@Component
class TopicResolver {

    fun resolve(eventType: String, version: Int): String =
        when (eventType to version) {
            "PaymentSucceeded" to 1 -> "payments.payment-succeeded.v1"
            "PaymentFailed" to 1 -> "payments.payment-failed.v1"
            else -> error("No topic mapping for $eventType v$version")
        }
}
