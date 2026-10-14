package br.com.fiap.paymentservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidPaymentException extends RuntimeException {

    public InvalidPaymentException() {
        super("Amount must be greater than zero");
    }

}
