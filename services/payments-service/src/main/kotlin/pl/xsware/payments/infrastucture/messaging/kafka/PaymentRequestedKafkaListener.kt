package pl.xsware.payments.infrastucture.messaging.kafka

import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import pl.xsware.payments.application.event.PaymentRequestedEvent
import pl.xsware.payments.application.service.PaymentService
import pl.xsware.payments.infrastucture.idempotency.RedisIdempotencyService
import pl.xsware.payments.infrastucture.logging.logger

@Component
class PaymentRequestedKafkaListener(
    private val paymentService: PaymentService,
    private val redisIdempotencyService: RedisIdempotencyService
) {

    private val log = logger()

    @KafkaListener(
        topics = ["payments.payment-requested.v1"],
        groupId = "payments-service"
    )
    fun onMessage(event: PaymentRequestedEvent) {

        log.info(
            "PAYMENT_REQUESTED_RECEIVED eventId={} orderId={}",
            event.eventId,
            event.data.orderId
        )

        if (!redisIdempotencyService.firstTime(event.eventId)) {
            log.info("Duplicate event ignored (redis) eventId={}", event.eventId)
            return
        }

        paymentService.process(event)
    }
}
