package pl.xsware.payments.infrastucture.persistence.outbox.entity

enum class OutboxStatus {
    PENDING,
    SENT,
    FAILED
}
