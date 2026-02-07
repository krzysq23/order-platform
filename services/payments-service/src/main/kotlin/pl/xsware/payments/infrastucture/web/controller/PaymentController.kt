package pl.xsware.payments.infrastucture.web.controller

import jakarta.validation.Valid
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import pl.xsware.payments.application.command.CreatePaymentRequestCommand
import pl.xsware.payments.application.service.PaymentService
import pl.xsware.payments.infrastucture.web.dto.CreatePaymentDto
import pl.xsware.payments.infrastucture.web.dto.PaymentDto
import java.util.UUID

@Profile("dev")
@RestController
@RequestMapping("/dev/payments")
class PaymentController(
    private val service: PaymentService
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody dto: CreatePaymentDto): PaymentDto {
        val result = service.create(
            CreatePaymentRequestCommand(
                orderId = dto.orderId!!,
                amount = dto.amount!!,
                currency = dto.currency!!,
                provider = dto.provider,
                externalId = dto.externalId
            )
        )
        return PaymentDto.from(result)
    }

    @GetMapping
    fun getByOrderId(@RequestParam orderId: UUID): PaymentDto =
        service.getByOrderId(orderId)?.let(PaymentDto::from)
            ?: throw org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found")
}
