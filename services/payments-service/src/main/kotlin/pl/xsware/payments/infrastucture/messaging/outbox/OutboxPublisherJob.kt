package pl.xsware.payments.infrastucture.messaging.outbox

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import pl.xsware.payments.infrastucture.logging.logger
import pl.xsware.payments.infrastucture.persistence.outbox.entity.OutboxStatus
import pl.xsware.payments.infrastucture.persistence.outbox.repository.OutboxClaimService
import pl.xsware.payments.infrastucture.persistence.outbox.repository.OutboxJpaRepository
import java.time.Instant
import java.time.temporal.ChronoUnit

@ConditionalOnProperty(name=["outbox.publisher.enabled"], havingValue="true", matchIfMissing=true)
@Component
class OutboxPublisherJob(
    private val claimService: OutboxClaimService,
    private val outboxRepo: OutboxJpaRepository,
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val topicResolver: TopicResolver
) {

    private val log = logger("OUTBOX")

    @Scheduled(
        fixedDelayString = "\${outbox.publisher.fixed-delay:PT2S}",
        initialDelayString = "\${outbox.publisher.initial-delay:PT5S}"
    )
    fun publish() {

        val batch = claimService.claimBatch(limit = 50, lockSeconds = 30)
        if (batch.isEmpty()) return

        batch.forEach { item ->
            try {
                val topic = topicResolver.resolve(item.eventType, item.eventVersion)
                val key = item.aggregateId.toString()

                kafkaTemplate.send(topic, key, item.payload).get()

                markSent(item.id)
                log.info("OUTBOX_SENT id={} topic={} type={} v={}", item.id, topic, item.eventType, item.eventVersion)
            } catch (ex: Exception) {
                markFailed(item.id, ex)
                log.warn("OUTBOX_SEND_FAILED id={} type={} v={} err={}", item.id, item.eventType, item.eventVersion, ex.message)
            }
        }
    }

    @Transactional
    fun markSent(id: java.util.UUID) {
        val e = outboxRepo.getReferenceById(id)
        e.status = OutboxStatus.SENT
        e.sentAt = Instant.now()
        e.lockedUntil = null
        e.lastError = null
    }

    @Transactional
    fun markFailed(id: java.util.UUID, ex: Exception) {
        val e = outboxRepo.getReferenceById(id)
        e.status = OutboxStatus.FAILED
        e.attempts += 1
        e.lockedUntil = Instant.now().plus(30, ChronoUnit.SECONDS)
        e.lastError = (ex.message ?: ex.javaClass.name).take(2000)
    }

}
