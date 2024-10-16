package com.reactivespring.controller;

import com.reactivespring.domain.Movie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Objects;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@AutoConfigureWireMock(port = 8084) // spin up a httpserver in port 8084
@TestPropertySource(properties = {
        "restClient.moviesInfoUrl: http://localhost:8084/v1/movieinfos",
        "restClient.reviewsUrl: http://localhost:8084/v1/reviews"})
public class MoviesControllerIntgTest {

    @Autowired
    WebTestClient webTestClient;

    @Test
    void fetchMovieById() {

        // given
        String movieId = "abc";
        stubFor(get(urlEqualTo("/v1/movieinfos" + "/" + movieId)).willReturn(aResponse().withHeader("Content-Type",
                                                                                                    "application/json")
                                                                                        .withBodyFile("movieinfo.json")));


        stubFor(get(urlPathEqualTo("/v1/reviews")).willReturn(aResponse().withHeader("Content-Type", "application/json")
                                                                         .withBodyFile("reviews.json")));


        // when
        webTestClient.get()
                     .uri("/v1/movies/{id}", movieId)
                     .exchange()
                     .expectStatus()
                     .isOk()
                     .expectBody(Movie.class)
                     .consumeWith(movieEntityExchangeResult -> {
                         Movie fetchedMovie = movieEntityExchangeResult.getResponseBody();
                         assert Objects.requireNonNull(fetchedMovie)
                                       .getReviewList()
                                       .size() == 2;
                         assertEquals("Batman Begins",
                                      fetchedMovie.getMovieInfo()
                                                  .getName());

                     });
    }

    @Test
    void fetchMovieById_404() {

        // given
        String movieId = "abc";

        stubFor(get(urlEqualTo("/v1/movieinfos" + "/" + movieId)).willReturn(aResponse().withStatus(404)));

        stubFor(get(urlPathEqualTo("/v1/reviews")).willReturn(aResponse().withHeader("Content-Type", "application/json")
                                                                         .withBodyFile("reviews.json")));

        // when
        webTestClient.get()
                     .uri("/v1/movies/{id}", movieId)
                     .exchange()
                     .expectStatus()
                     .is4xxClientError()
                     .expectBody(String.class)
                     .isEqualTo("There is no movieInfo available for the passed in Id : abc");
    }

    @Test
    void fetchMovieById_reviews_404() {

        // given
        String movieId = "abc";

        stubFor(get(urlEqualTo("/v1/movieinfos" + "/" + movieId)).willReturn(aResponse().withHeader("Content-Type",
                                                                                                    "application/json")
                                                                                        .withBodyFile("movieinfo.json")));

        stubFor(get(urlPathEqualTo("/v1/reviews")).willReturn(aResponse().withStatus(404)));

        // when
        webTestClient.get()
                     .uri("/v1/movies/{id}", movieId)
                     .exchange()
                     .expectStatus()
                     .isOk()
                     .expectBody(Movie.class)
                     .consumeWith(movieEntityExchangeResult -> {
                         Movie fetchedMovie = movieEntityExchangeResult.getResponseBody();
                         assert Objects.requireNonNull(fetchedMovie)
                                       .getReviewList()
                                       .isEmpty();
                         assertEquals("Batman Begins",
                                      fetchedMovie.getMovieInfo()
                                                  .getName());
                     });
    }

    @Test
    void fetchMovieById_5XX() {

        // given
        String movieId = "abc";

        stubFor(get(urlEqualTo("/v1/movieinfos" + "/" + movieId)).willReturn(aResponse().withStatus(500)
                                                                                        .withBody(
                                                                                                "MovieInfo Service Unavailable")));

        /*stubFor(get(urlPathEqualTo("/v1/reviews")).willReturn(aResponse().withHeader("Content-Type", "application/json")
                                                                         .withBodyFile("reviews.json")));*/

        // when
        webTestClient.get()
                     .uri("/v1/movies/{id}", movieId)
                     .exchange()
                     .expectStatus()
                     .is5xxServerError()
                     .expectBody(String.class)
                     .isEqualTo("Server Exception in MoviesInfoService MovieInfo Service Unavailable");
    }

    @Test
    void fetchMovieById_reviews_5XX() {

        // given
        String movieId = "abc";

        stubFor(get(urlEqualTo("/v1/movieinfos" + "/" + movieId)).willReturn(aResponse().withHeader("Content-Type",
                                                                                                    "application/json")
                                                                                        .withBodyFile("movieinfo.json")));

        stubFor(get(urlPathEqualTo("/v1/reviews")).willReturn(aResponse().withStatus(500)
                                                                         .withBody("Unavailable")));

        // when
        webTestClient.get()
                     .uri("/v1/movies/{id}", movieId)
                     .exchange()
                     .expectStatus()
                     .is5xxServerError()
                     .expectBody(String.class)
                     .isEqualTo("Server Exception in ReviewsService Unavailable");
//                     .isEqualTo("Server Exception in ReviewsService MovieInfo Service Unavailable");
    }
}
