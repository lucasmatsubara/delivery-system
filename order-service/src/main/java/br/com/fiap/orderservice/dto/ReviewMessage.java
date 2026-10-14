package br.com.fiap.orderservice.dto;

public record ReviewMessage(Long dishId, String dishName, int rating, String comment) {}
