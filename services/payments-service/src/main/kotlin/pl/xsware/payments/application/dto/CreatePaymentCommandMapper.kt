package pl.xsware.payments.application.dto

import pl.xsware.payments.application.command.CreatePaymentCommand
import pl.xsware.payments.application.event.PaymentRequestedEvent
import java.math.BigDecimal

fun PaymentRequestedEvent.toCreatePaymentCommand(
    provider: String? = null,
    externalId: String? = null
): CreatePaymentCommand {

    return CreatePaymentCommand(
        orderId = data.orderId,
        amount = BigDecimal(data.amount).setScale(2),
        currency = data.currency,
        provider = provider,
        externalId = externalId
    )
}
