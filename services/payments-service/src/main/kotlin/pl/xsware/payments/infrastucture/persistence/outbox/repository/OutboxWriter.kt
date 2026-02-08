package pl.xsware.payments.infrastucture.persistence.outbox.repository

import org.springframework.stereotype.Component
import pl.xsware.payments.infrastucture.persistence.outbox.entity.OutboxEntity
import pl.xsware.payments.infrastucture.persistence.outbox.entity.OutboxStatus
import tools.jackson.databind.ObjectMapper
import java.time.Instant
import java.util.UUID

@Component
class OutboxWriter(
    private val objectMapper: ObjectMapper,
    private val outboxRepo: OutboxJpaRepository
) {
    fun add(
        aggregateType: String,
        aggregateId: UUID,
        eventType: String,
        eventVersion: Int,
        occurredAt: Instant,
        payloadObj: Any
    ) {
        val payloadJson = objectMapper.writeValueAsString(payloadObj)

        outboxRepo.save(
            OutboxEntity(
                id = UUID.randomUUID(),
                aggregateType = aggregateType,
                aggregateId = aggregateId,
                eventType = eventType,
                eventVersion = eventVersion,
                occurredAt = occurredAt,
                payload = payloadJson,
                status = OutboxStatus.PENDING
            )
        )
    }
}
