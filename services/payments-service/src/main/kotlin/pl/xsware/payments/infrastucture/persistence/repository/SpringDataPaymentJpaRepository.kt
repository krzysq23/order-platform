package pl.xsware.payments.infrastucture.persistence.repository

import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import pl.xsware.payments.infrastucture.persistence.entity.PaymentEntity
import java.util.UUID

interface SpringDataPaymentJpaRepository : JpaRepository<PaymentEntity, UUID> {

    fun findByOrderId(orderId: UUID): PaymentEntity?

    @Query(
        """
        SELECT p FROM PaymentEntity p
        ORDER BY p.createdAt DESC
        """
    )
    fun findLatest(pageable: Pageable): List<PaymentEntity>
}
