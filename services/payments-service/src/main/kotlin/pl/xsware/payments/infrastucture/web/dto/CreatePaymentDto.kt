package pl.xsware.payments.infrastucture.web.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import java.math.BigDecimal
import java.util.UUID

data class CreatePaymentDto(

    @field:NotNull val orderId: UUID?,
    @field:NotNull @field:Positive val amount: BigDecimal?,
    @field:NotBlank val currency: String?,
    val provider: String? = null,
    val externalId: String? = null
)
