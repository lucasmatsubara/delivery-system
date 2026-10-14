package br.com.fiap.orderservice.dto;

public record ReviewRequest(Long dishId, Integer rating, String comment) {}
