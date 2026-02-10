package pl.xsware.payments.application.service

import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import pl.xsware.payments.application.event.PaymentAuthorizedEvent
import pl.xsware.payments.application.event.PaymentCancelledEvent
import pl.xsware.payments.application.event.PaymentFailedEvent
import pl.xsware.payments.application.event.PaymentSucceededEvent
import pl.xsware.payments.domain.model.Payment
import pl.xsware.payments.domain.port.PaymentRepository
import pl.xsware.payments.infrastucture.persistence.outbox.repository.OutboxWriter
import java.time.Clock
import java.time.Instant
import java.util.UUID

@Service
class PaymentStatusUseCase(
    private val paymentRepo: PaymentRepository,
    private val outboxWriter: OutboxWriter,
    private val clock: Clock
) {

    @Transactional
    fun authorize(paymentId: UUID, provider: String, externalId: String? = null) {

        val now = Instant.now(clock)

        val payment: Payment = paymentRepo.findById(paymentId)

        payment.markAuthorized(provider = provider, externalId = externalId, clock = clock)
        paymentRepo.save(payment)

        outboxWriter.add(
            aggregateType = "Payment",
            aggregateId = payment.id,
            eventType = PaymentAuthorizedEvent.TYPE,
            eventVersion = PaymentAuthorizedEvent.VERSION,
            occurredAt = now,
            payloadObj = PaymentAuthorizedEvent(
                eventId = UUID.randomUUID(),
                eventType = PaymentAuthorizedEvent.TYPE,
                version = PaymentAuthorizedEvent.VERSION,
                occurredAt = now,
                data = PaymentAuthorizedEvent.Data(
                    paymentId = payment.id,
                    orderId = payment.orderId,
                    provider = provider
                )
            )
        )
    }

    @Transactional
    fun capture(paymentId: UUID, provider: String, externalId: String) {

        val now = Instant.now(clock)

        val payment: Payment = paymentRepo.findById(paymentId)

        payment.markCaptured(provider = provider, externalId = externalId, clock = clock)
        paymentRepo.save(payment)

        outboxWriter.add(
            aggregateType = "Payment",
            aggregateId = payment.id,
            eventType = PaymentSucceededEvent.TYPE,
            eventVersion = PaymentSucceededEvent.VERSION,
            occurredAt = now,
            payloadObj = PaymentSucceededEvent(
                eventId = UUID.randomUUID(),
                eventType = PaymentSucceededEvent.TYPE,
                version = PaymentSucceededEvent.VERSION,
                occurredAt = now,
                data = PaymentSucceededEvent.Data(
                    paymentId = payment.id,
                    orderId = payment.orderId,
                    amount = payment.amount,
                    currency = payment.currency,
                    provider = provider,
                    externalId = externalId
                )
            )
        )
    }

    @Transactional
    fun cancel(paymentId: UUID, reason: String? = null) {

        val now = Instant.now(clock)

        val payment: Payment = paymentRepo.findById(paymentId)

        payment.markCancelled(clock = clock)
        paymentRepo.save(payment)

        outboxWriter.add(
            aggregateType = "Payment",
            aggregateId = payment.id,
            eventType = PaymentCancelledEvent.TYPE,
            eventVersion = PaymentCancelledEvent.VERSION,
            occurredAt = now,
            payloadObj = PaymentCancelledEvent(
                eventId = UUID.randomUUID(),
                eventType = PaymentCancelledEvent.TYPE,
                version = PaymentCancelledEvent.VERSION,
                occurredAt = now,
                data = PaymentCancelledEvent.Data(
                    paymentId = payment.id,
                    orderId = payment.orderId,
                    reason = reason
                )
            )
        )
    }

    @Transactional
    fun fail(paymentId: UUID, reason: String? = null) {

        val now = Instant.now(clock)

        val payment: Payment = paymentRepo.findById(paymentId)

        payment.markFailed(clock = clock)
        paymentRepo.save(payment)

        outboxWriter.add(
            aggregateType = "Payment",
            aggregateId = payment.id,
            eventType = PaymentFailedEvent.TYPE,
            eventVersion = PaymentFailedEvent.VERSION,
            occurredAt = now,
            payloadObj = PaymentFailedEvent(
                eventId = UUID.randomUUID(),
                eventType = PaymentFailedEvent.TYPE,
                version = PaymentFailedEvent.VERSION,
                occurredAt = now,
                data = PaymentFailedEvent.Data(
                    paymentId = payment.id,
                    orderId = payment.orderId,
                    reason = reason
                )
            )
        )
    }

}
