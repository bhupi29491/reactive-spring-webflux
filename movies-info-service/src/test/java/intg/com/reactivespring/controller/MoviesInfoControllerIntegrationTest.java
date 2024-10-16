package com.reactivespring.controller;

import com.reactivespring.domain.MovieInfo;
import com.reactivespring.repository.MovieInfoRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
class MoviesInfoControllerIntegrationTest {

    static String MOVIES_INFO_URI = "/v1/movieinfos";
    @Autowired
    MovieInfoRepository movieInfoRepository;
    @Autowired
    WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        List<MovieInfo> movieInfos = List.of(new MovieInfo(null,
                                                           "Batman Begins",
                                                           2005,
                                                           List.of("Christian Bale", "Michael Cane"),
                                                           LocalDate.parse("2005-06-15")),
                                             new MovieInfo(null,
                                                           "The Dark Knight",
                                                           2008,
                                                           List.of("Christian Bale", "HeathLedger"),
                                                           LocalDate.parse("2008-07-18")),
                                             new MovieInfo("abc",
                                                           "Dark Knight Rises",
                                                           2012,
                                                           List.of("Christian Bale", "Tom Hardy"),
                                                           LocalDate.parse("2012-07-20")));

        movieInfoRepository.saveAll(movieInfos)
                           .blockLast();
    }

    @AfterEach
    void tearDown() {
        movieInfoRepository.deleteAll()
                           .block();
    }

    @Test
    void addMovieInfo() {
        //given
        MovieInfo movieInfo = new MovieInfo(null,
                                            "Thor: Love and Thunder",
                                            2022,
                                            List.of("Christian Bale", "Natalie Portman"),
                                            LocalDate.parse("2022-07-06"));

        //when
        webTestClient.post()
                     .uri(MOVIES_INFO_URI)
                     .bodyValue(movieInfo)
                     .exchange()
                     .expectStatus()
                     .isCreated()
                     .expectBody(MovieInfo.class)
                     .consumeWith(movieInfoEntityExchangeResult -> {
                         MovieInfo savedMovieInfo = movieInfoEntityExchangeResult.getResponseBody();
                         assert savedMovieInfo != null;
                         assert savedMovieInfo.getMovieInfoId() != null;
                     });

    }

    @Test
    void getAllMovieInfos_Stream() {

        MovieInfo movieInfo = new MovieInfo(null,
                                            "Thor: Love and Thunder",
                                            2022,
                                            List.of("Christian Bale", "Natalie Portman"),
                                            LocalDate.parse("2022-07-06"));

        //when
        webTestClient.post()
                     .uri(MOVIES_INFO_URI)
                     .bodyValue(movieInfo)
                     .exchange()
                     .expectStatus()
                     .isCreated()
                     .expectBody(MovieInfo.class)
                     .consumeWith(movieInfoEntityExchangeResult -> {
                         MovieInfo savedMovieInfo = movieInfoEntityExchangeResult.getResponseBody();
                         assert savedMovieInfo != null;
                         assert savedMovieInfo.getMovieInfoId() != null;
                     });

        //when
        Flux<MovieInfo> movieStreamFlux = webTestClient.get()
                                                       .uri(MOVIES_INFO_URI + "/stream")
                                                       .exchange()
                                                       .expectStatus()
                                                       .is2xxSuccessful()
                                                       .returnResult(MovieInfo.class)
                                                       .getResponseBody();
        StepVerifier.create(movieStreamFlux)
                    .assertNext(movieInfo1 -> {
                        assert movieInfo1.getMovieInfoId() != null;
                    })
                    .thenCancel()
                    .verify();
    }

    @Test
    void getAllMovieInfos() {

        //when
        webTestClient.get()
                     .uri(MOVIES_INFO_URI)
                     .exchange()
                     .expectStatus()
                     .is2xxSuccessful()
                     .expectBodyList(MovieInfo.class)
                     .hasSize(3);
    }

    @Test
    void getMovieInfoByYear() {

        URI uri = UriComponentsBuilder.fromUriString(MOVIES_INFO_URI)
                                      .queryParam("year", 2005)
                                      .buildAndExpand()
                                      .toUri();

        //when
        webTestClient.get()
                     .uri(uri)
                     .exchange()
                     .expectStatus()
                     .is2xxSuccessful()
                     .expectBodyList(MovieInfo.class)
                     .hasSize(1);
    }

    @Test
    void getMovieInfoByName() {

        URI uri = UriComponentsBuilder.fromUriString(MOVIES_INFO_URI)
                                      .queryParam("name", "Dark Knight Rises")
                                      .buildAndExpand()
                                      .toUri();

        //when
        webTestClient.get()
                     .uri(uri)
                     .exchange()
                     .expectStatus()
                     .is2xxSuccessful()
                     .expectBodyList(MovieInfo.class)
                     .hasSize(1);
    }


    @Test
    void getMovieInfoByID() {
        //given
        String movieInfoId = "abc";

        //when
        webTestClient.get()
                     .uri(MOVIES_INFO_URI + "/{id}", movieInfoId)
                     .exchange()
                     .expectStatus()
                     .is2xxSuccessful()
                     .expectBody()
                     .consumeWith(entityExchangeResult -> {
                         byte[] responseBody = entityExchangeResult.getResponseBody();
                         assertNotNull(responseBody);
                     });

    }

    @Test
    void getMovieInfoByID_Approach2() {
        //given
        String movieInfoId = "abc";

        //when
        webTestClient.get()
                     .uri(MOVIES_INFO_URI + "/{id}", movieInfoId)
                     .exchange()
                     .expectStatus()
                     .is2xxSuccessful()
                     .expectBody()
                     .jsonPath("$.name")
                     .isEqualTo("Dark Knight Rises");
    }

    @Test
    void updateMovieInfo() {
        //given
        String movieInfoId = "abc";
        MovieInfo movieInfo = new MovieInfo(null,
                                            "Thor: Love and Thunder",
                                            2022,
                                            List.of("Christian Bale", "Natalie Portman"),
                                            LocalDate.parse("2022-07-06"));

        //when
        webTestClient.put()
                     .uri(MOVIES_INFO_URI + "/{id}", movieInfoId)
                     .bodyValue(movieInfo)
                     .exchange()
                     .expectStatus()
                     .is2xxSuccessful()
                     .expectBody(MovieInfo.class)
                     .consumeWith(movieInfoEntityExchangeResult -> {
                         MovieInfo updatedMovieInfo = movieInfoEntityExchangeResult.getResponseBody();
                         assert updatedMovieInfo != null;
                         assert updatedMovieInfo.getMovieInfoId() != null;
                         assertEquals("Thor: Love and Thunder", updatedMovieInfo.getName());
                     });

    }

    @Test
    void deleteMovieInfo() {
        //given
        String movieInfoId = "abc";

        //when
        webTestClient.delete()
                     .uri(MOVIES_INFO_URI + "/{id}", movieInfoId)
                     .exchange()
                     .expectStatus()
                     .isNoContent();
    }

    @Test
    void updateMovieInfo_NotFound() {
        //given
        String movieInfoId = "def";
        MovieInfo movieInfo = new MovieInfo(null,
                                            "Thor: Love and Thunder",
                                            2022,
                                            List.of("Christian Bale", "Natalie Portman"),
                                            LocalDate.parse("2022-07-06"));

        //when
        webTestClient.put()
                     .uri(MOVIES_INFO_URI + "/{id}", movieInfoId)
                     .bodyValue(movieInfo)
                     .exchange()
                     .expectStatus()
                     .isNotFound();
    }

    @Test
    void getMovieInfoByID_NotFound() {
        //given
        String movieInfoId = "def";

        //when
        webTestClient.get()
                     .uri(MOVIES_INFO_URI + "/{id}", movieInfoId)
                     .exchange()
                     .expectStatus()
                     .isNotFound();

    }


}