package br.com.fiap.reviewservice.service;

import br.com.fiap.reviewservice.config.RabbitConfig;
import br.com.fiap.reviewservice.dto.RankingResponse;
import br.com.fiap.reviewservice.dto.ReviewMessage;
import br.com.fiap.reviewservice.model.ReviewStats;
import br.com.fiap.reviewservice.model.ReviewSummary;
import br.com.fiap.reviewservice.repository.ReviewSummaryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class ReviewService {

    private final ReviewSummaryRepository reviewSummaryRepository;
    private final ConcurrentHashMap<Long, ReviewStats> buffer = new ConcurrentHashMap<>();

    public ReviewService(ReviewSummaryRepository reviewSummaryRepository) {
        this.reviewSummaryRepository = reviewSummaryRepository;
    }

    @RabbitListener(queues = RabbitConfig.QUEUE_NAME)
    public void processReview(ReviewMessage message) {
        if (message.dishId() == null || message.rating() == null || message.rating() < 1 || message.rating() > 5) {
            log.warn("Discarding invalid review: {}", message);
            throw new AmqpRejectAndDontRequeueException("Invalid review message");
        }
        buffer.merge(message.dishId(), ReviewStats.of(message.dishName(), message.rating()), ReviewStats::plus);
    }

    @Scheduled(fixedDelay = 5_000)
    public void flush() {
        if (buffer.isEmpty()) return;

        long total = 0;
        for (Long dishId : buffer.keySet()) {
            ReviewStats stats = buffer.remove(dishId);
            if (stats == null) continue;
            try {
                saveStats(dishId, stats);
                total += stats.count();
            } catch (RuntimeException e) {
                log.error("Could not save reviews of dish {}, keeping them for the next flush", dishId, e);
                buffer.merge(dishId, stats, ReviewStats::plus);
            }
        }
        log.info("Flush: {} reviews saved to the database", total);
    }

    public List<RankingResponse> getRanking() {
        return reviewSummaryRepository.findAllByOrderByAverageDescReviewCountDesc().stream()
                .map(RankingResponse::from)
                .toList();
    }

    private void saveStats(Long dishId, ReviewStats stats) {
        var summary = reviewSummaryRepository.findById(dishId)
                .orElseGet(() -> new ReviewSummary(dishId, stats.dishName(), 0, 0, 0));
        summary.add(stats);
        reviewSummaryRepository.save(summary);
    }

}
