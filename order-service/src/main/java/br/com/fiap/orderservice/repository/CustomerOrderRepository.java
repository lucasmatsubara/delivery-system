package br.com.fiap.orderservice.repository;

import br.com.fiap.orderservice.model.CustomerOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, Long> {
}
