package br.com.fiap.orderservice.dto;

import java.math.BigDecimal;

public record PaymentRequest(BigDecimal amount) {}
