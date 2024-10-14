package com.reactivespring.handler;

import com.reactivespring.domain.Review;
import com.reactivespring.exception.ReviewDataException;
import com.reactivespring.exception.ReviewNotFoundException;
import com.reactivespring.repository.ReviewReactiveRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ReviewHandler {

    @Autowired
    private Validator validator;

    private ReviewReactiveRepository reviewReactiveRepository;

    public ReviewHandler(ReviewReactiveRepository reviewReactiveRepository) {
        this.reviewReactiveRepository = reviewReactiveRepository;
    }

    private static Mono<ServerResponse> buildReviewsResponse(Flux<Review> reviewsFlux) {
        return ServerResponse.ok()
                             .body(reviewsFlux, Review.class);
    }

    public Mono<ServerResponse> addReview(ServerRequest request) {

        return request.bodyToMono(Review.class)
                      .doOnNext(this::validate)
                      .flatMap(review -> reviewReactiveRepository.save(review))
                      .flatMap(ServerResponse.status(HttpStatus.CREATED)::bodyValue);
    }

    private void validate(Review review) {

        Set<ConstraintViolation<Review>> constraintViolations = validator.validate(review);
        log.info("constraintViolations : {}", constraintViolations);

        if (!constraintViolations.isEmpty()) {
            String errorMessage = constraintViolations.stream()
                                                      .map(ConstraintViolation::getMessage)
                                                      .sorted()
                                                      .collect(Collectors.joining(","));
            throw new ReviewDataException(errorMessage);
        }
    }

    public Mono<ServerResponse> getReviews(ServerRequest request) {

        Optional<String> movieInfoId = request.queryParam("movieInfoId");
        if (movieInfoId.isPresent()) {
            Flux<Review> reviewsByMovieInfoId = reviewReactiveRepository.findReviewsByMovieInfoId(Long.valueOf(
                    movieInfoId.get()));
            return buildReviewsResponse(reviewsByMovieInfoId);
        } else {
            Flux<Review> reviewFlux = reviewReactiveRepository.findAll();
            return buildReviewsResponse(reviewFlux);
        }

    }

    public Mono<ServerResponse> updateReview(ServerRequest request) {

        String reviewId = request.pathVariable("id");

        Mono<Review> existingReview = reviewReactiveRepository.findById(reviewId)
                                                              .switchIfEmpty(Mono.error(new ReviewNotFoundException(
                                                                      "Review not found for the given Review id : " + reviewId)));

        return existingReview.flatMap(review -> request.bodyToMono(Review.class)
                                                       .map(reqReview -> {
                                                           review.setComment(reqReview.getComment());
                                                           review.setRating(reqReview.getRating());
                                                           return review;
                                                       })
                                                       .flatMap(reviewReactiveRepository::save)
                                                       .flatMap(savedReview -> ServerResponse.ok()
                                                                                             .bodyValue(savedReview)));
    }

    public Mono<ServerResponse> deleteReview(ServerRequest request) {

        String reviewId = request.pathVariable("id");
        Mono<Review> existingReview = reviewReactiveRepository.findById(reviewId);

        return existingReview.flatMap(review -> reviewReactiveRepository.deleteById(reviewId))
                             .then(ServerResponse.noContent()
                                                 .build());

    }
}
