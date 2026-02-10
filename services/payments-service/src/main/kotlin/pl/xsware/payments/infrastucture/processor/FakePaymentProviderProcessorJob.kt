package pl.xsware.payments.infrastucture.processor

import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import pl.xsware.payments.application.service.PaymentStatusUseCase
import pl.xsware.payments.infrastucture.idempotency.RedisLockService
import pl.xsware.payments.infrastucture.persistence.payment.repository.JpaPaymentRepositoryAdapter
import java.time.Duration
import java.util.UUID
import kotlin.random.Random

@Component
class FakePaymentProviderProcessorJob(
    private val paymentRepoAdapter: JpaPaymentRepositoryAdapter,
    private val paymentStatusUseCase: PaymentStatusUseCase,
    private val lockService: RedisLockService,
    @Value("\${payments.fake-provider.enabled:true}") private val enabled: Boolean,
    @Value("\${payments.fake-provider.batch-size:10}") private val batchSize: Int,
    @Value("\${payments.fake-provider.success-rate:0.8}") private val successRate: Double,
    @Value("\${payments.fake-provider.provider-name:FAKE_PSP}") private val providerName: String,
    @Value("\${payments.fake-provider.lock-ttl:PT30S}") private val lockTtl: String
) {

    @Scheduled(
        fixedDelayString = "\${payments.fake-provider.fixed-delay:PT2S}",
        initialDelayString = "\${payments.fake-provider.initial-delay:PT5S}"
    )
    fun run() {
        if (!enabled) return

        val lock = lockService.tryLock(
            name = "fake-payment-processor",
            ttl = Duration.parse(lockTtl)
        ) ?: return

        try {
            val ids = paymentRepoAdapter.findNextRequestedIds(batchSize)
            if (ids.isEmpty()) return

            ids.forEach { paymentId ->
                processOne(paymentId)
            }
        } finally {
            lockService.unlock(lock)
        }
    }

    private fun processOne(paymentId: UUID) {
        // 1) AUTH
        paymentStatusUseCase.authorize(
            paymentId = paymentId,
            provider = providerName,
            externalId = null
        )

        // 2) CAPTURE albo FAIL
        val ok = Random.nextDouble(0.0, 1.0) < successRate
        if (ok) {
            paymentStatusUseCase.capture(
                paymentId = paymentId,
                provider = providerName,
                externalId = "trx_${paymentId}"
            )
        } else {
            paymentStatusUseCase.fail(
                paymentId = paymentId,
                reason = "DECLINED"
            )
        }
    }
}
