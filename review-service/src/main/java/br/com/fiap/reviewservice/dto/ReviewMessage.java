package br.com.fiap.reviewservice.dto;

public record ReviewMessage(Long dishId, String dishName, Integer rating, String comment) {}
