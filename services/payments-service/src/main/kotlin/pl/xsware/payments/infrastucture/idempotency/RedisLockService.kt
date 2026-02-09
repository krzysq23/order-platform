package pl.xsware.payments.infrastucture.idempotency

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.script.DefaultRedisScript
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.UUID

@Component
class RedisLockService(
    private val redis: StringRedisTemplate
) {

    fun tryLock(name: String, ttl: Duration): LockHandle? {
        val key = "lock:$name"
        val token = UUID.randomUUID().toString()
        val acquired = redis.opsForValue().setIfAbsent(key, token, ttl) == true
        return if (acquired) LockHandle(key, token) else null
    }

    fun unlock(handle: LockHandle) {
        val lua = """
            if redis.call("get", KEYS[1]) == ARGV[1] then
              return redis.call("del", KEYS[1])
            else
              return 0
            end
        """.trimIndent()

        val script = DefaultRedisScript(lua, Long::class.java)
        redis.execute(script, listOf(handle.key), handle.token)
    }

    data class LockHandle(val key: String, val token: String)
}
