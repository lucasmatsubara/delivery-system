package br.com.fiap.reviewservice.model;

public record ReviewStats(String dishName, long ratingSum, long count) {

    public static ReviewStats of(String dishName, int rating) {
        return new ReviewStats(dishName, rating, 1);
    }

    public ReviewStats plus(ReviewStats other) {
        return new ReviewStats(other.dishName(), ratingSum + other.ratingSum(), count + other.count());
    }

}
