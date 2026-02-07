package pl.xsware.payments.domain.model

enum class PaymentStatus {

    REQUESTED,
    AUTHORIZED,
    CAPTURED,
    FAILED,
    CANCELLED
}
