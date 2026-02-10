package pl.xsware.payments

import com.fasterxml.jackson.databind.ObjectMapper
import com.redis.testcontainers.RedisContainer
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import pl.xsware.payments.infrastucture.messaging.outbox.OutboxPublisherJob
import pl.xsware.payments.infrastucture.persistence.outbox.entity.OutboxEntity
import pl.xsware.payments.infrastucture.persistence.outbox.entity.OutboxStatus
import pl.xsware.payments.infrastucture.persistence.outbox.repository.OutboxJpaRepository
import java.time.Instant
import java.util.UUID
import java.util.concurrent.CompletableFuture

@SpringBootTest
@Testcontainers
class OutboxPublisherJobIT {

    companion object {
        @Container
        val postgres = PostgreSQLContainer("postgres:16-alpine")
            .withDatabaseName("payments")
            .withUsername("payments")
            .withPassword("payments")

        @Container
        val redis = RedisContainer("redis:7-alpine")

        @JvmStatic
        @DynamicPropertySource
        fun props(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)

            registry.add("spring.data.redis.host", redis::getHost)
            registry.add("spring.data.redis.port") { redis.firstMappedPort }

            registry.add("outbox.publisher.enabled") { "true" }
        }
    }

    @Autowired
    lateinit var job: OutboxPublisherJob

    @Autowired
    lateinit var outboxRepo: OutboxJpaRepository

    @Autowired
    lateinit var redisTemplate: StringRedisTemplate

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @MockitoBean
    lateinit var kafkaTemplate: KafkaTemplate<String, String>

    @BeforeEach
    fun clean() {
        outboxRepo.deleteAll()
        redisTemplate.delete("lock:outbox-publisher")
    }

    @Test
    fun `publish() should acquire redis lock, send to kafka, and mark outbox as SENT`() {
        // given: outbox record PENDING
        val outboxId = UUID.randomUUID()
        outboxRepo.save(
            OutboxEntity(
                id = outboxId,
                aggregateType = "Payment",
                aggregateId = UUID.randomUUID(),
                eventType = "PaymentSucceeded",
                eventVersion = 1,
                occurredAt = Instant.now(),
                payload = objectMapper.valueToTree(mapOf("hello" to "world")),
                status = OutboxStatus.PENDING
            )
        )

        // and: kafka send future completes OK
        whenever(
            kafkaTemplate.send(any<String>(), any<String>(), any<String>())
        ).thenReturn(CompletableFuture.completedFuture(null))

        // when
        job.publish()

        // then: kafka send called once
        verify(kafkaTemplate).send(any<String>(), any<String>(), any<String>())

        // and: outbox marked SENT
        val saved = outboxRepo.findById(outboxId).orElseThrow()
        assert(saved.status == OutboxStatus.SENT)
        assert(saved.sentAt != null)
    }

    @Test
    fun `publish() should do nothing when redis lock is held by another instance`() {

        // given: another instance holds lock
        redisTemplate.opsForValue().set("lock:outbox-publisher", "foreign-token")

        // and: outbox record exists
        outboxRepo.save(
            OutboxEntity(
                id = UUID.randomUUID(),
                aggregateType = "Payment",
                aggregateId = UUID.randomUUID(),
                eventType = "PaymentSucceeded",
                eventVersion = 1,
                occurredAt = Instant.now(),
                payload = objectMapper.valueToTree(mapOf("hello" to "world")),
                status = OutboxStatus.PENDING
            )
        )

        // when
        job.publish()

        // then: no kafka send
        verify(kafkaTemplate, never()).send(any<String>(), any<String>(), any<String>())
    }
}
