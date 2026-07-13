package com.hafiz5007.kyc.observability

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.UUID

/**
 * Per-request structured log line with a correlation id + latency.
 * Mirrors the request/response logging filter from the original Java service.
 */
@Component
class RequestLoggingFilter : OncePerRequestFilter() {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        chain: FilterChain
    ) {
        val cid = request.getHeader(CORRELATION_HEADER) ?: UUID.randomUUID().toString()
        response.setHeader(CORRELATION_HEADER, cid)
        MDC.put("cid", cid)

        val start = System.nanoTime()
        try {
            chain.doFilter(request, response)
        } finally {
            val ms = (System.nanoTime() - start) / 1_000_000
            log.info(
                "{} {} -> {} in {}ms",
                request.method, request.requestURI, response.status, ms
            )
            MDC.remove("cid")
        }
    }

    companion object {
        const val CORRELATION_HEADER = "X-Correlation-Id"
    }
}
