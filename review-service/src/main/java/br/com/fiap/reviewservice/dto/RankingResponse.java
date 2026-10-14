package br.com.fiap.reviewservice.dto;

import br.com.fiap.reviewservice.model.ReviewSummary;

public record RankingResponse(Long dishId, String dishName, double average, long count) {

    public static RankingResponse from(ReviewSummary summary) {
        double average = Math.round(summary.getAverage() * 10.0) / 10.0;
        return new RankingResponse(summary.getDishId(), summary.getDishName(), average, summary.getReviewCount());
    }

}
