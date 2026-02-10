package pl.xsware.payments.domain.port

import pl.xsware.payments.domain.model.Payment
import java.util.UUID

interface PaymentRepository {

    fun save(payment: Payment): Payment
    fun findById(id: UUID): Payment
    fun findByOrderId(orderId: UUID): Payment?
    fun findLatest(limit: Int): List<Payment>
}
