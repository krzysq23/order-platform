package pl.xsware.payments.infrastucture.persistence.payment.mapper

import pl.xsware.payments.domain.model.Payment
import pl.xsware.payments.domain.model.PaymentStatus
import pl.xsware.payments.infrastucture.persistence.payment.entity.PaymentEntity


object PaymentMapper {

    fun toEntity(p: Payment) = PaymentEntity(
        id = p.id,
        orderId = p.orderId,
        status = p.status.name,
        amount = p.amount,
        currency = p.currency,
        provider = p.provider,
        externalId = p.externalId,
        createdAt = p.createdAt,
        updatedAt = p.updatedAt
    )

    fun toDomain(e: PaymentEntity) = Payment(
        id = e.id,
        orderId = e.orderId,
        status = PaymentStatus.valueOf(e.status),
        amount = e.amount,
        currency = e.currency,
        provider = e.provider,
        externalId = e.externalId,
        createdAt = e.createdAt,
        updatedAt = e.updatedAt
    )
}
