package br.com.fiap.orderservice.client;

import br.com.fiap.orderservice.dto.PaymentRequest;
import br.com.fiap.orderservice.dto.PaymentResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

@Slf4j
@Component
public class PaymentClient {

    private static final String PAYMENT_URL = "http://PAYMENT-SERVICE/payments";

    private final RestTemplate restTemplate;

    public PaymentClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Retryable(
            includes = {RestClientException.class, IllegalStateException.class},
            maxRetries = 4,
            delay = 300,
            multiplier = 2,
            jitter = 100,
            maxDelay = 2000
    )
    public PaymentResponse pay(BigDecimal amount) {
        try {
            PaymentResponse response = restTemplate.postForObject(PAYMENT_URL, new PaymentRequest(amount), PaymentResponse.class);
            if (response == null || !"APPROVED".equals(response.status())) {
                throw new IllegalStateException("Payment not approved");
            }
            log.info("Payment of {} APPROVED by payment-service instance {}", amount, response.instance());
            return response;
        } catch (HttpStatusCodeException e) {
            log.warn("Payment attempt failed with {}: {}", e.getStatusCode().value(), e.getResponseBodyAsString());
            throw e;
        } catch (RestClientException | IllegalStateException e) {
            log.warn("Payment attempt failed: {}", e.getMessage());
            throw e;
        }
    }

}
