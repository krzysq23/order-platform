package pl.xsware.payments.infrastucture.idempotency

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.UUID

@Component
class RedisIdempotencyService(
    private val redis: StringRedisTemplate
) {

    fun firstTime(eventId: UUID, ttl: Duration = Duration.ofHours(24)): Boolean {
        val key = "inbox:event:$eventId"
        return redis.opsForValue().setIfAbsent(key, "1", ttl) == true
    }
}
