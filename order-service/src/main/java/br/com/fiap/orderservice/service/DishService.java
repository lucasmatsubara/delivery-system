package br.com.fiap.orderservice.service;

import br.com.fiap.orderservice.exception.DishNotFoundException;
import br.com.fiap.orderservice.model.Dish;
import br.com.fiap.orderservice.repository.DishRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DishService {

    private final DishRepository dishRepository;

    public DishService(DishRepository dishRepository) {
        this.dishRepository = dishRepository;
    }

    public List<Dish> findAll() {
        return dishRepository.findAllByOrderByIdAsc();
    }

    public Dish findById(Long id) {
        return dishRepository.findById(id).orElseThrow(() -> new DishNotFoundException(id));
    }

}
