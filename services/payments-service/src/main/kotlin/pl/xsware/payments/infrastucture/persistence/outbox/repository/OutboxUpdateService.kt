package pl.xsware.payments.infrastucture.persistence.outbox.repository

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pl.xsware.payments.infrastucture.persistence.outbox.entity.OutboxStatus
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

@Service
class OutboxUpdateService(
    private val outboxRepo: OutboxJpaRepository
) {

    @Transactional
    fun markSent(id: UUID) {
        val e = outboxRepo.findById(id).orElseThrow() // <- nie proxy
        e.status = OutboxStatus.SENT
        e.sentAt = Instant.now()
        e.lockedUntil = null
        e.lastError = null
    }

    @Transactional
    fun markFailed(id: UUID, ex: Exception) {
        val e = outboxRepo.findById(id).orElseThrow()
        e.status = OutboxStatus.FAILED
        e.attempts += 1
        e.lockedUntil = Instant.now().plus(30, ChronoUnit.SECONDS)
        e.lastError = (ex.message ?: ex.javaClass.name).take(2000)
    }
}
