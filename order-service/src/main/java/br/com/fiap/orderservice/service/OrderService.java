package br.com.fiap.orderservice.service;

import br.com.fiap.orderservice.client.PaymentClient;
import br.com.fiap.orderservice.dto.OrderRequest;
import br.com.fiap.orderservice.exception.DishNotFoundException;
import br.com.fiap.orderservice.exception.InvalidRequestException;
import br.com.fiap.orderservice.exception.OrderNotFoundException;
import br.com.fiap.orderservice.exception.OutOfStockException;
import br.com.fiap.orderservice.exception.PaymentUnavailableException;
import br.com.fiap.orderservice.model.CustomerOrder;
import br.com.fiap.orderservice.model.Dish;
import br.com.fiap.orderservice.model.OrderStatus;
import br.com.fiap.orderservice.repository.CustomerOrderRepository;
import br.com.fiap.orderservice.repository.DishRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Slf4j
@Service
public class OrderService {

    private final DishRepository dishRepository;
    private final CustomerOrderRepository orderRepository;
    private final PaymentClient paymentClient;

    public OrderService(DishRepository dishRepository, CustomerOrderRepository orderRepository,
                        PaymentClient paymentClient) {
        this.dishRepository = dishRepository;
        this.orderRepository = orderRepository;
        this.paymentClient = paymentClient;
    }

    @Transactional
    public CustomerOrder create(OrderRequest request) {
        validate(request);

        Dish dish = dishRepository.findByIdForUpdate(request.dishId())
                .orElseThrow(() -> new DishNotFoundException(request.dishId()));

        int quantity = request.quantity();
        if (dish.getStock() < quantity) {
            log.info("Order rejected: dish {} has {} left, {} requested", dish.getId(), dish.getStock(), quantity);
            throw new OutOfStockException();
        }

        BigDecimal totalPrice = dish.getPrice().multiply(BigDecimal.valueOf(quantity));

        try {
            paymentClient.pay(totalPrice);
        } catch (RuntimeException e) {
            log.warn("Order rejected: payment failed after all retries ({})", e.getMessage());
            throw new PaymentUnavailableException();
        }

        dish.setStock(dish.getStock() - quantity);
        CustomerOrder order = orderRepository.save(new CustomerOrder(null, dish.getId(), quantity, totalPrice,
                OrderStatus.CONFIRMED, LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)));

        log.info("Order {} CONFIRMED: {} x {} (stock left: {})", order.getId(), quantity, dish.getName(), dish.getStock());
        return order;
    }

    public CustomerOrder findById(Long id) {
        return orderRepository.findById(id).orElseThrow(() -> new OrderNotFoundException(id));
    }

    private void validate(OrderRequest request) {
        if (request == null || request.dishId() == null) {
            throw new InvalidRequestException("dishId is required");
        }
        if (request.quantity() == null || request.quantity() < 1) {
            throw new InvalidRequestException("Quantity must be at least 1");
        }
    }

}
