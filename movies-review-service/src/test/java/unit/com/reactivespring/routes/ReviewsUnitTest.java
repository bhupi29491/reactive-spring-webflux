package com.reactivespring.routes;

import com.reactivespring.domain.Review;
import com.reactivespring.handler.ReviewHandler;
import com.reactivespring.repository.ReviewReactiveRepository;
import com.reactivespring.router.ReviewRouter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;

@WebFluxTest
@ContextConfiguration(classes = {ReviewRouter.class, ReviewHandler.class})
@AutoConfigureWebTestClient
public class ReviewsUnitTest {

    static String REVIEWS_URI = "/v1/reviews";

    @Autowired
    WebTestClient webTestClient;
    @MockBean
    private ReviewReactiveRepository reviewReactiveRepositoryMock;

    @Test
    public void addReview() {

        // given
        Review awesomeMovie = new Review(null, 1L, "Awesome Movie", 9.0);

        // when
        when(reviewReactiveRepositoryMock.save(isA(Review.class))).thenReturn(Mono.just(new Review("abc",
                                                                                                   1L,
                                                                                                   "Awesome Movie",
                                                                                                   9.0)));

        // then
        webTestClient
                .post()
                .uri(REVIEWS_URI)
                .bodyValue(awesomeMovie)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(Review.class)
                .consumeWith(reviewEntityExchangeResult -> {
                    Review savedReview = reviewEntityExchangeResult.getResponseBody();
                    assert savedReview != null;
                    assert savedReview.getReviewId() != null;
                });
    }

    @Test
    public void getReviews() {

        // given
        List<Review> reviewsList = List.of(new Review(null, 1L, "Awesome Movie", 9.0),
                                           new Review(null, 1L, "Awesome Movie1", 9.0),
                                           new Review(null, 2L, "Excellent Movie", 8.0));

        // when
        when(reviewReactiveRepositoryMock.findAll()).thenReturn(Flux.fromIterable(reviewsList));

        // then
        webTestClient
                .get()
                .uri(REVIEWS_URI)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(Review.class)
                .hasSize(3);

    }


    @Test
    void updateReview() {
        //given
        Review reviewUpdate = new Review(null, 1L, "Awesome Movie", 9.0);


        //when
        when(reviewReactiveRepositoryMock.save(isA(Review.class))).thenReturn(Mono.just(new Review("abc",
                                                                                                   1L,
                                                                                                   "Not an  Awesome Movie",
                                                                                                   8.0)));

        when(reviewReactiveRepositoryMock.findById(isA(String.class))).thenReturn(Mono.just(new Review("abc",
                                                                                                       1L,
                                                                                                       "Awesome Movie",
                                                                                                       9.0)));


        webTestClient
                .put()
                .uri(REVIEWS_URI + "/{id}", "abc")
                .bodyValue(reviewUpdate)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Review.class)
                .consumeWith(reviewEntityExchangeResult -> {
                    Review updatedReview = reviewEntityExchangeResult.getResponseBody();
                    assert updatedReview != null;
                    System.out.println("updatedReview : " + updatedReview);
                    assertEquals(8.0, updatedReview.getRating());
                    assertEquals("Not an  Awesome Movie", updatedReview.getComment());
                });

    }


    @Test
    public void deleteReview() {

        // given
        String reviewId = "abc";

        // when
        when(reviewReactiveRepositoryMock.findById((String) any())).thenReturn(Mono.just(new Review("abc",
                                                                                                    1L,
                                                                                                    "Awesome Movie",
                                                                                                    9.0)));

        when(reviewReactiveRepositoryMock.deleteById((String) any())).thenReturn(Mono.empty());


        // then
        webTestClient
                .delete()
                .uri(REVIEWS_URI + "/{id}", "abc")
                .exchange()
                .expectStatus()
                .isNoContent();
    }


}
