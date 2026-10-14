package br.com.fiap.paymentservice.dto;

import java.math.BigDecimal;

public record PaymentRequest(BigDecimal amount) {}
