package br.com.fiap.paymentservice.exception;

import br.com.fiap.paymentservice.dto.ErrorResponse;
import br.com.fiap.paymentservice.service.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final PaymentService paymentService;

    public GlobalExceptionHandler(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @ExceptionHandler(PaymentFailureException.class)
    public ResponseEntity<ErrorResponse> handlePaymentFailure(PaymentFailureException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(e.getMessage(), paymentService.getInstancePort()));
    }

    @ExceptionHandler(InvalidPaymentException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPayment(InvalidPaymentException e) {
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(e.getMessage(), paymentService.getInstancePort()));
    }

}
