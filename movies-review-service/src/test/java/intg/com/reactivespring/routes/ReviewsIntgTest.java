package com.reactivespring.routes;

import com.reactivespring.domain.Review;
import com.reactivespring.repository.ReviewReactiveRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
public class ReviewsIntgTest {

    static String REVIEWS_URI = "/v1/reviews";

    @Autowired
    WebTestClient webTestClient;

    @Autowired
    ReviewReactiveRepository reviewReactiveRepository;

    @BeforeEach
    void setUp() {
        List<Review> reviewsList = List.of(new Review(null, 1L, "Awesome Movie", 9.0),
                                           new Review(null, 1L, "Awesome Movie1", 9.0),
                                           new Review(null, 2L, "Excellent Movie", 8.0));

        reviewReactiveRepository
                .saveAll(reviewsList)
                .blockLast();
    }

    @AfterEach
    void tearDown() {
        reviewReactiveRepository
                .deleteAll()
                .block();
    }

    @Test
    public void addReview() {

        // given
        Review awesomeMovie = new Review(null, 1L, "Awesome Movie", 9.0);

        // when
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

        // when
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
        Review awesomeMovie = new Review(null, 1L, "Awesome Movie", 9.0);
        Review savedReview = reviewReactiveRepository
                .save(awesomeMovie)
                .block();
        Review reviewUpdate = new Review(null, 1L, "Not an  Awesome Movie", 8.0);

        //when
        assert savedReview != null;

        webTestClient
                .put()
                .uri(REVIEWS_URI + "/{id}", savedReview.getReviewId())
                .bodyValue(reviewUpdate)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(Review.class)
                .consumeWith(reviewEntityExchangeResult -> {
                    Review updatedReview = reviewEntityExchangeResult.getResponseBody();
                    assert updatedReview != null;
                    System.out.println("updatedReview : " + updatedReview);
                    assertNotNull(savedReview.getReviewId());
                    assertEquals("Not an  Awesome Movie", updatedReview.getComment());
                });

    }

    @Test
    public void deleteReview() {

        // given
        Review awesomeMovie = new Review(null, 1L, "Awesome Movie", 9.0);
        Review savedReview = reviewReactiveRepository
                .save(awesomeMovie)
                .block();

        // when
        assert savedReview != null;
        webTestClient
                .delete()
                .uri(REVIEWS_URI + "/{id}", savedReview.getReviewId())
                .exchange()
                .expectStatus()
                .isNoContent();
    }

    @Test
    void getReviewsByMovieInfoId() {

        URI uri = UriComponentsBuilder
                .fromUriString(REVIEWS_URI)
                .queryParam("movieInfoId", "1")
                .buildAndExpand()
                .toUri();

        //when
        webTestClient
                .get()
                .uri(uri)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(Review.class)
                .value(reviewList -> {
                    System.out.println("reviewList : " + reviewList);
                    assertEquals(2, reviewList.size());
                });
    }


}
