package pl.xsware.payments.infrastucture.persistence.payment.repository

import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Repository
import pl.xsware.payments.domain.model.Payment
import pl.xsware.payments.domain.model.PaymentStatus
import pl.xsware.payments.domain.port.PaymentRepository
import pl.xsware.payments.infrastucture.persistence.payment.mapper.PaymentMapper
import java.util.UUID

@Repository
class JpaPaymentRepositoryAdapter(
    private val jpa: SpringDataPaymentJpaRepository
) : PaymentRepository {

    override fun save(payment: Payment): Payment {
        jpa.save(PaymentMapper.toEntity(payment))
        return payment
    }

    override fun findById(id: UUID): Payment {
        val entity = jpa.findById(id)
            .orElseThrow { IllegalArgumentException("Payment not found: $id") }
        return PaymentMapper.toDomain(entity)
    }

    override fun findByOrderId(orderId: UUID): Payment? {
        return jpa.findByOrderId(orderId)?.let(PaymentMapper::toDomain)
    }

    override fun findLatest(limit: Int): List<Payment> {
        return jpa
            .findLatest(PageRequest.of(0, limit))
            .map(PaymentMapper::toDomain)
    }

    fun findNextRequestedIds(limit: Int): List<UUID> =
        jpa.findByStatusOrderByCreatedAtAsc(PaymentStatus.REQUESTED, PageRequest.of(0, limit))
            .map { it.id }
}
