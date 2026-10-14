package br.com.fiap.orderservice.config;

import br.com.fiap.orderservice.ratelimit.RateLimitInterceptor;
import br.com.fiap.orderservice.ratelimit.RateLimiter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${delivery.rate-limit.enabled:true}")
    private boolean rateLimitEnabled;

    @Value("${delivery.rate-limit.requests-per-second:20}")
    private int requestsPerSecond;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "OPTIONS")
                .allowedHeaders("*");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        if (rateLimitEnabled) {
            registry.addInterceptor(new RateLimitInterceptor(new RateLimiter(requestsPerSecond)))
                    .addPathPatterns("/orders");
        }
    }

}
