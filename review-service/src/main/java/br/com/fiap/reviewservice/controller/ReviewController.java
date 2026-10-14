package br.com.fiap.reviewservice.controller;

import br.com.fiap.reviewservice.dto.RankingResponse;
import br.com.fiap.reviewservice.service.ReviewService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/ranking")
    public List<RankingResponse> ranking() {
        return reviewService.getRanking();
    }

}
