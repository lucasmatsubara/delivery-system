package br.com.fiap.orderservice.repository;

import br.com.fiap.orderservice.model.Dish;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface DishRepository extends JpaRepository<Dish, Long> {

    List<Dish> findAllByOrderByIdAsc();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from Dish d where d.id = :id")
    Optional<Dish> findByIdForUpdate(Long id);

}
