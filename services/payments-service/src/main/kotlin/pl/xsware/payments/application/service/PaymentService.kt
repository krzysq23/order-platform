package pl.xsware.payments.application.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pl.xsware.payments.application.command.CreatePaymentRequestCommand
import pl.xsware.payments.application.dto.PaymentResult
import pl.xsware.payments.domain.model.Payment
import pl.xsware.payments.domain.port.PaymentRepository
import pl.xsware.payments.infrastucture.logging.logger
import java.util.UUID

@Service
class PaymentService(
    private val repo: PaymentRepository
) {

    private val log = logger()

    @Transactional
    fun create(cmd: CreatePaymentRequestCommand): PaymentResult {

        log.info("Creating payment for orderId={}, amount={} {}", cmd.orderId, cmd.amount, cmd.currency)
        val payment = Payment.request(
            orderId = cmd.orderId,
            amount = cmd.amount,
            currency = cmd.currency,
            provider = cmd.provider,
            externalId = cmd.externalId
        )
        repo.save(payment)
        log.info("Payment created id={}, orderId={}, status={}", payment.id, payment.orderId, payment.status)
        return PaymentResult.from(payment)
    }

    fun getById(id: UUID): PaymentResult? {
        log.debug("Fetching payment by id={}", id)
        return repo.findById(id)?.let(PaymentResult::from)
    }

    fun getByOrderId(orderId: UUID): PaymentResult? {
        log.debug("Fetching payment by orderId={}", orderId)
        return repo.findByOrderId(orderId)?.let(PaymentResult::from)
    }

}
