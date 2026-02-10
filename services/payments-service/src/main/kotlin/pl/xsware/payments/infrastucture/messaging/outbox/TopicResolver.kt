package pl.xsware.payments.infrastucture.messaging.outbox

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import pl.xsware.payments.application.event.PaymentCancelledEvent
import pl.xsware.payments.application.event.PaymentFailedEvent
import pl.xsware.payments.application.event.PaymentRequestedEvent
import pl.xsware.payments.application.event.PaymentSucceededEvent

@Component
class TopicResolver(

    @Value("\${app.kafka.topics.payment-requested}")
    private val paymentRequestedTopic: String,

    @Value("\${app.kafka.topics.payment-succeeded}")
    private val paymentSucceededTopic: String,

    @Value("\${app.kafka.topics.payment-failed}")
    private val paymentFailedTopic: String,

    @Value("\${app.kafka.topics.payment-cancelled}")
    private val paymentCancelledTopic: String
) {

    fun resolve(eventType: String, version: Int): String =
        when (eventType to version) {

            PaymentRequestedEvent.TYPE to PaymentRequestedEvent.VERSION ->
                paymentRequestedTopic

            PaymentSucceededEvent.TYPE to PaymentSucceededEvent.VERSION ->
                paymentSucceededTopic

            PaymentFailedEvent.TYPE to PaymentFailedEvent.VERSION ->
                paymentFailedTopic

            PaymentCancelledEvent.TYPE to PaymentCancelledEvent.VERSION ->
                paymentCancelledTopic

            else -> error("No topic mapping for $eventType v$version")
        }
}
