package pl.xsware.payments.infrastucture.persistence.repository

import org.springframework.data.jpa.repository.JpaRepository
import pl.xsware.payments.infrastucture.persistence.entity.PaymentEntity
import java.util.UUID

interface SpringDataPaymentJpaRepository : JpaRepository<PaymentEntity, UUID> {

    fun findByOrderId(orderId: UUID): PaymentEntity?
}
