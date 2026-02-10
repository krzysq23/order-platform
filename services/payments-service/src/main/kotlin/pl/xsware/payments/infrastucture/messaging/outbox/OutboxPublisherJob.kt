package pl.xsware.payments.infrastucture.messaging.outbox

import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.header.internals.RecordHeader
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import pl.xsware.payments.infrastucture.idempotency.RedisLockService
import pl.xsware.payments.infrastucture.logging.logger
import pl.xsware.payments.infrastucture.persistence.outbox.repository.OutboxClaimService
import pl.xsware.payments.infrastucture.persistence.outbox.repository.OutboxUpdateService
import java.nio.charset.StandardCharsets
import java.time.Duration

@ConditionalOnProperty(name=["outbox.publisher.enabled"], havingValue="true", matchIfMissing=true)
@Component
class OutboxPublisherJob(
    private val claimService: OutboxClaimService,
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val topicResolver: TopicResolver,
    private val lockService: RedisLockService,
    private val outboxUpdateService: OutboxUpdateService
) {

    private val log = logger("OUTBOX")

    @Value("\${outbox.publisher.lock-ttl:PT60S}")
    private lateinit var lockTtl: Duration

    @Scheduled(
        fixedDelayString = "\${outbox.publisher.fixed-delay:PT2S}",
        initialDelayString = "\${outbox.publisher.initial-delay:PT5S}"
    )
    fun publish() {
        val lock = lockService.tryLock(
            name = "outbox-publisher",
            ttl = lockTtl
        ) ?: run {
            log.debug("OUTBOX_LOCK_NOT_ACQUIRED")
            return
        }

        try {
            publishOnce()
        } finally {
            lockService.unlock(lock)
        }
    }

    private fun publishOnce() {
        val batch = claimService.claimBatch(limit = 50, lockSeconds = 30)
        if (batch.isEmpty()) return

        batch.forEach { item ->
            try {
                val topic = topicResolver.resolve(item.eventType, item.eventVersion)
                val key = item.aggregateId.toString()
                val payload = item.payload.toString()
                val record = ProducerRecord<String, String>(topic, key, payload)

                record.headers().add(
                    RecordHeader("eventId", item.id.toString().toByteArray(StandardCharsets.UTF_8))
                )
                record.headers().add(
                    RecordHeader("eventType", item.eventType.toByteArray(StandardCharsets.UTF_8))
                )
                record.headers().add(
                    RecordHeader("eventVersion", item.eventVersion.toString().toByteArray(StandardCharsets.UTF_8))
                )
                record.headers().add(
                    RecordHeader("occurredAt", item.occurredAt.toString().toByteArray(StandardCharsets.UTF_8))
                )

                kafkaTemplate.send(record).get()

                outboxUpdateService.markSent(item.id)

                log.info("OUTBOX_SENT id={} topic={} type={} v={}", item.id, topic, item.eventType, item.eventVersion)
            } catch (ex: Exception) {
                outboxUpdateService.markFailed(item.id, ex)
                log.warn(
                    "OUTBOX_SEND_FAILED id={} type={} v={} err={}",
                    item.id, item.eventType, item.eventVersion, ex.message
                )
            }
        }
    }

}
