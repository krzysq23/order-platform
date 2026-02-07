package pl.xsware.payments.application.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pl.xsware.payments.application.command.CreatePaymentRequestCommand
import pl.xsware.payments.application.dto.PaymentResult
import pl.xsware.payments.domain.model.Payment
import pl.xsware.payments.domain.port.PaymentRepository
import java.util.UUID

@Service
class PaymentService(
    private val repo: PaymentRepository
) {

    @Transactional
    fun create(cmd: CreatePaymentRequestCommand): PaymentResult {
        val p = Payment.request(
            orderId = cmd.orderId,
            amount = cmd.amount,
            currency = cmd.currency,
            provider = cmd.provider,
            externalId = cmd.externalId
        )
        repo.save(p)
        return PaymentResult.from(p)
    }

    fun getById(id: UUID): PaymentResult? =
        repo.findById(id)?.let(PaymentResult::from)

    fun getByOrderId(orderId: UUID): PaymentResult? =
        repo.findByOrderId(orderId)?.let(PaymentResult::from)
}
