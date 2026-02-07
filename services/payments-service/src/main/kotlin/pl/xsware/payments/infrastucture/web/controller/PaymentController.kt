package pl.xsware.payments.infrastucture.web.controller

import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import pl.xsware.payments.application.command.CreatePaymentRequestCommand
import pl.xsware.payments.application.service.PaymentService
import pl.xsware.payments.infrastucture.web.dto.CreatePaymentDto
import pl.xsware.payments.infrastucture.web.dto.PaymentDto
import java.util.UUID

@RestController
@RequestMapping("/payments")
class PaymentController(
    private val service: PaymentService
) {
    private val http = LoggerFactory.getLogger("HTTP")

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody dto: CreatePaymentDto): PaymentDto {
        val start = System.currentTimeMillis()
        val result = service.create(
            CreatePaymentRequestCommand(
                orderId = dto.orderId!!,
                amount = dto.amount!!,
                currency = dto.currency!!,
                provider = dto.provider,
                externalId = dto.externalId
            )
        )
        http.info("POST /payments orderId={} -> 201 ({} ms)", dto.orderId, System.currentTimeMillis() - start)
        return PaymentDto.from(result)
    }

    @GetMapping("/{id}")
    fun getById(@PathVariable id: UUID): PaymentDto =
        service.getById(id)?.let(PaymentDto::from)
            ?: throw org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found")

    @GetMapping
    fun getByOrderId(@RequestParam orderId: UUID): PaymentDto =
        service.getByOrderId(orderId)?.let(PaymentDto::from)
            ?: throw org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found")
}
