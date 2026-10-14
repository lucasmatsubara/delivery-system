package br.com.fiap.orderservice.ratelimit;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Bonus: POST /orders above the limit gets 429 Too Many Requests with {"error": "..."}.
 */
@Slf4j
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimiter rateLimiter;

    public RateLimitInterceptor(RateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!"POST".equalsIgnoreCase(request.getMethod()) || rateLimiter.tryAcquire()) {
            return true;
        }
        log.warn("Rate limit exceeded for POST /orders ({} requests per second)", rateLimiter.getLimit());
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Retry-After", "1");
        response.getWriter().write("{\"error\":\"Too many requests: limit is " + rateLimiter.getLimit() + " orders per second\"}");
        return false;
    }

}
