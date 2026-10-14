package br.com.fiap.orderservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_GATEWAY)
public class AssistantUnavailableException extends RuntimeException {

    public AssistantUnavailableException() {
        super("Assistant unavailable");
    }

}
