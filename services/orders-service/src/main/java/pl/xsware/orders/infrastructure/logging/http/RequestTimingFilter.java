package pl.xsware.orders.infrastructure.logging.http;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class RequestTimingFilter extends OncePerRequestFilter {

    private static final String MDC_REQUEST_ID = "requestId";
    private static final String HEADER_REQUEST_ID = "X-Request-Id";

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {

        long startNs = System.nanoTime();

        String method = request.getMethod();
        String path = request.getRequestURI();
        String query = request.getQueryString();
        String fullPath = (query == null) ? path : path + "?" + query;

        String requestId = resolveOrCreateRequestId(request);
        MDC.put(MDC_REQUEST_ID, requestId);
        response.setHeader(HEADER_REQUEST_ID, requestId);

        log.debug("HTTP_IN  {} {} from={} ua={}", method, fullPath, request.getRemoteAddr(), request.getHeader("User-Agent"));


        try {
            filterChain.doFilter(request, response);
        } finally {
            long tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);

            int status = response.getStatus();

            if (tookMs >= 1000) {
                log.warn("{} {} -> {} ({} ms)", method, fullPath, status, tookMs);
            } else {
                log.info("{} {} -> {} ({} ms)", method, fullPath, status, tookMs);
            }

            MDC.remove(MDC_REQUEST_ID);
        }
    }

    private String resolveOrCreateRequestId(HttpServletRequest request) {
        String header = request.getHeader(HEADER_REQUEST_ID);
        if (header != null && !header.isBlank()) {
            return header.trim();
        }
        return UUID.randomUUID().toString();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/actuator");
    }

}
