package pl.xsware.payments.infrastucture.web.controller

import jakarta.validation.Valid
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import pl.xsware.payments.application.command.CreatePaymentCommand
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
            CreatePaymentCommand(
                orderId = dto.orderId!!,
                amount = dto.amount!!,
                currency = dto.currency!!,
                provider = dto.provider,
                externalId = dto.externalId
            )
        )
        return PaymentDto.from(result)
    }

    @GetMapping("/byOrder")
    fun getByOrderId(@RequestParam orderId: UUID): PaymentDto =
        service.getByOrderId(orderId)?.let(PaymentDto::from)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found")


    @GetMapping("/latest")
    fun getByLastOrders(@RequestParam(defaultValue = "10") count: Int): List<PaymentDto> {
        require(count in 1..100) { "count must be between 1 and 100" }
        return service.getByLastOrders(count)?.map(PaymentDto::from)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found")
    }

}
