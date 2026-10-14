package br.com.fiap.orderservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_GATEWAY)
public class PaymentUnavailableException extends RuntimeException {

    public PaymentUnavailableException() {
        super("Payment service unavailable");
    }

}
