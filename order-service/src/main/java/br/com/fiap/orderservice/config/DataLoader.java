package br.com.fiap.orderservice.config;

import br.com.fiap.orderservice.model.Dish;
import br.com.fiap.orderservice.repository.DishRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Component
public class DataLoader implements CommandLineRunner {

    private final DishRepository dishRepository;

    public DataLoader(DishRepository dishRepository) {
        this.dishRepository = dishRepository;
    }

    @Override
    public void run(String... args) {
        if (dishRepository.count() > 0) return;

        dishRepository.saveAll(List.of(
                new Dish(null, "House Burger",
                        "Flash sale! Brioche bun, 180g beef patty, cheddar and caramelized onions",
                        new BigDecimal("39.90"), 10),
                new Dish(null, "Margherita Pizza",
                        "Vegetarian. Tomato sauce, mozzarella and fresh basil",
                        new BigDecimal("49.90"), 30),
                new Dish(null, "Veggie Bowl",
                        "Vegan. Quinoa, chickpeas, avocado and roasted vegetables",
                        new BigDecimal("34.90"), 25),
                new Dish(null, "Chicken Parmigiana",
                        "Breaded chicken breast with tomato sauce and mozzarella, served with rice and fries",
                        new BigDecimal("44.90"), 20),
                new Dish(null, "Salmon Poke",
                        "Fresh salmon, sushi rice, cucumber, edamame and sesame",
                        new BigDecimal("54.90"), 15),
                new Dish(null, "Mushroom Risotto",
                        "Vegetarian. Arborio rice, mixed mushrooms and parmesan",
                        new BigDecimal("46.90"), 12),
                new Dish(null, "Chocolate Brownie",
                        "Vegetarian. Warm brownie with vanilla ice cream",
                        new BigDecimal("19.90"), 40)
        ));
        log.info("Menu loaded with {} dishes (flash sale dish starts with stock 10)", dishRepository.count());
    }

}
