package br.com.fiap.orderservice.service;

import br.com.fiap.orderservice.config.RabbitConfig;
import br.com.fiap.orderservice.dto.ReviewMessage;
import br.com.fiap.orderservice.dto.ReviewRequest;
import br.com.fiap.orderservice.exception.DishNotFoundException;
import br.com.fiap.orderservice.exception.InvalidRequestException;
import br.com.fiap.orderservice.model.Dish;
import br.com.fiap.orderservice.repository.DishRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ReviewService {

    private static final int MAX_COMMENT_LENGTH = 500;

    private final DishRepository dishRepository;
    private final RabbitTemplate rabbitTemplate;

    public ReviewService(DishRepository dishRepository, RabbitTemplate rabbitTemplate) {
        this.dishRepository = dishRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publish(ReviewRequest request) {
        if (request == null || request.dishId() == null) {
            throw new InvalidRequestException("dishId is required");
        }
        if (request.rating() == null || request.rating() < 1 || request.rating() > 5) {
            throw new InvalidRequestException("Rating must be between 1 and 5");
        }
        if (request.comment() != null && request.comment().length() > MAX_COMMENT_LENGTH) {
            throw new InvalidRequestException("Comment must have at most " + MAX_COMMENT_LENGTH + " characters");
        }

        Dish dish = dishRepository.findById(request.dishId())
                .orElseThrow(() -> new DishNotFoundException(request.dishId()));

        var message = new ReviewMessage(dish.getId(), dish.getName(), request.rating(), request.comment());
        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE_NAME, RabbitConfig.ROUTING_KEY, message);
        log.info("Review published for dish {} with rating {}", dish.getId(), request.rating());
    }

}
