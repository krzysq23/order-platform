package pl.xsware.payments.infrastucture.messaging.kafka

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import pl.xsware.payments.application.event.PaymentRequestedEvent
import pl.xsware.payments.application.service.PaymentService

@Component
class PaymentRequestedKafkaListener(
    private val objectMapper: ObjectMapper,
    private val paymentService: PaymentService
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @KafkaListener(
        topics = ["payments.payment-requested.v1"],
        groupId = "payments-service"
    )
    fun onMessage(payload: String) {

        val event = try {
            objectMapper.readValue(payload, PaymentRequestedEvent::class.java)
        } catch (ex: Exception) {
            log.error("Cannot deserialize PaymentRequestedEvent payload={}", payload, ex)
            throw ex
        }

        log.info(
            "PAYMENT_REQUESTED_RECEIVED eventId={} orderId={}",
            event.eventId,
            event.data.orderId
        )

        paymentService.process(event)
    }
}
