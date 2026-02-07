package pl.xsware.payments.application.command

import java.math.BigDecimal
import java.util.UUID

data class CreatePaymentCommand(
    val orderId: UUID,
    val amount: BigDecimal,
    val currency: String,
    val provider: String? = null,
    val externalId: String? = null
)
