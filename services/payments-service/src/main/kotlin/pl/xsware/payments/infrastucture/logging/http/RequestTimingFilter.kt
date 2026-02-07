package pl.xsware.payments.infrastucture.logging.http

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import pl.xsware.payments.infrastucture.logging.logger
import kotlin.system.measureTimeMillis

@Component
class RequestTimingFilter : OncePerRequestFilter() {

    private val log = logger("HTTP")

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val method = request.method
        val path = request.requestURI

        val tookMs = measureTimeMillis {
            filterChain.doFilter(request, response)
        }

        val status = response.status

        log.info(
            "{} {} -> {} ({} ms)",
            method,
            path,
            status,
            tookMs
        )
    }
}
