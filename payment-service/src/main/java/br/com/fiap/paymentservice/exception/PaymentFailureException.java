package br.com.fiap.paymentservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class PaymentFailureException extends RuntimeException {

    public PaymentFailureException() {
        super("Simulated payment failure");
    }

}
