package br.com.fiap.reviewservice.repository;

import br.com.fiap.reviewservice.model.ReviewSummary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewSummaryRepository extends JpaRepository<ReviewSummary, Long> {

    List<ReviewSummary> findAllByOrderByAverageDescReviewCountDesc();

}
