package br.com.fiap.paymentservice.service;

import br.com.fiap.paymentservice.dto.PaymentResponse;
import br.com.fiap.paymentservice.exception.InvalidPaymentException;
import br.com.fiap.paymentservice.exception.PaymentFailureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Random;

@Slf4j
@Service
public class PaymentService {

    private final Random random = new Random();
    private final Environment environment;

    public PaymentService(Environment environment) {
        this.environment = environment;
    }

    public PaymentResponse pay(BigDecimal amount) {
        int instance = getInstancePort();

        if (amount == null || amount.signum() <= 0) {
            throw new InvalidPaymentException();
        }

        if (random.nextBoolean()) {
            log.warn("[instance {}] Payment FAILED (simulated) for amount {}", instance, amount);
            throw new PaymentFailureException();
        }

        log.info("[instance {}] Payment APPROVED for amount {}", instance, amount);
        return new PaymentResponse("APPROVED", instance);
    }

    public int getInstancePort() {
        String port = environment.getProperty("local.server.port",
                environment.getProperty("server.port", "8081"));
        return Integer.parseInt(port);
    }

}
