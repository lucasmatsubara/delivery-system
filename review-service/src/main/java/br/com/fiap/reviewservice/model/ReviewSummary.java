package br.com.fiap.reviewservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewSummary {

    @Id
    private Long dishId;

    private String dishName;

    private long ratingSum;

    @Column(name = "review_count")
    private long reviewCount;

    private double average;

    public void add(ReviewStats stats) {
        dishName = stats.dishName();
        ratingSum += stats.ratingSum();
        reviewCount += stats.count();
        average = (double) ratingSum / reviewCount;
    }

}
