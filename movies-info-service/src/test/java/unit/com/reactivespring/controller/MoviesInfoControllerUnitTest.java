package com.reactivespring.controller;


import com.reactivespring.domain.MovieInfo;
import com.reactivespring.service.MovieInfoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;


@WebFluxTest(controllers = MoviesInfoController.class)
@AutoConfigureWebTestClient
public class MoviesInfoControllerUnitTest {

    static String MOVIES_INFO_URI = "/v1/movieinfos";

    @Autowired
    WebTestClient webTestClient;

    @MockBean
    private MovieInfoService movieInfoServiceMock;

    @Test
    void getAllMovieInfo() {

        //given
        List<MovieInfo> movieInfos = List.of(
                new MovieInfo(null, "Batman Begins", 2005, List.of("Christian Bale", "Michael Cane"),
                        LocalDate.parse("2005-06-15")),
                new MovieInfo(null, "The Dark Knight", 2008, List.of("Christian Bale", "HeathLedger"),
                        LocalDate.parse("2008-07-18")),
                new MovieInfo("abc", "Dark Knight Rises", 2012, List.of("Christian Bale", "Tom Hardy"),
                        LocalDate.parse("2012-07-20")));

        //when
        when(movieInfoServiceMock.getAllMovieInfos()).thenReturn(Flux.fromIterable(movieInfos));

        //then
        webTestClient.get()
                     .uri(MOVIES_INFO_URI)
                     .exchange()
                     .expectStatus()
                     .is2xxSuccessful()
                     .expectBodyList(MovieInfo.class)
                     .hasSize(3);
    }

    @Test
    void getMovieInfoByID() {
        //given
        String movieInfoId = "abc";

        MovieInfo movieInfo = new MovieInfo("abc", "Dark Knight Rises", 2012, List.of("Christian Bale", "Tom Hardy"),
                LocalDate.parse("2012-07-20"));

        //when
        when(movieInfoServiceMock.getMovieInfoById(isA(String.class))).thenReturn(Mono.just(movieInfo));

        //then
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
    void addMovieInfo() {
        //given
        MovieInfo movieInfo = new MovieInfo(null, "Thor: Love and Thunder", 2022,
                List.of("Christian Bale", "Natalie Portman"), LocalDate.parse("2022-07-06"));

        //when
        when(movieInfoServiceMock.addMovieInfo(isA(MovieInfo.class))).thenReturn(Mono.just(
                new MovieInfo("mockId", "Thor: Love and Thunder", 2022, List.of("Christian Bale", "Natalie Portman"),
                        LocalDate.parse("2022-07-06"))));

        //then
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
                         assertEquals("mockId", savedMovieInfo.getMovieInfoId());
                     });
    }

    @Test
    void updateMovieInfo() {
        //given
        String movieInfoId = "abc";
        MovieInfo movieInfo = new MovieInfo(null, "Thor: Love and Thunder", 2022,
                List.of("Christian Bale", "Natalie Portman"), LocalDate.parse("2022-07-06"));

        //when
        when(movieInfoServiceMock.updateMovieInfo(isA(MovieInfo.class), isA(String.class))).thenReturn(Mono.just(
                new MovieInfo(movieInfoId, "Thor: Love and Thunder", 2022, List.of("Christian Bale", "Natalie Portman"),
                        LocalDate.parse("2022-07-06"))));

        //then
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
        when(movieInfoServiceMock.deleteMovieInfo(isA(String.class))).thenReturn(Mono.empty());

        //then
        webTestClient.delete()
                     .uri(MOVIES_INFO_URI + "/{id}", movieInfoId)
                     .exchange()
                     .expectStatus()
                     .isNoContent();
    }

    @Test
    void addMovieInfo_Validation() {
        //given  Thor: Love and Thunder
        MovieInfo movieInfo = new MovieInfo(null, "", -2022, List.of(""), LocalDate.parse("2022-07-06"));


        //then
        webTestClient.post()
                     .uri(MOVIES_INFO_URI)
                     .bodyValue(movieInfo)
                     .exchange()
                     .expectStatus()
                     .isBadRequest()
                     .expectBody(String.class)
                     .consumeWith(stringEntityExchangeResult -> {
                         String responseBody = stringEntityExchangeResult.getResponseBody();
                         System.out.println("responseBody : " + responseBody);
                         String expectedErrorMsg = "movieInfo.cast must be present,movieInfo.name must be present,movieInfo.year must be a Positive value";
                         assert responseBody != null;
                         assertEquals(expectedErrorMsg, responseBody);
                     });
    }

    @Test
    void updateMovieInfo_Validation() {
        //given
        String movieInfoId = "abc";
        MovieInfo movieInfo = new MovieInfo(null, "", -2022, List.of(""), LocalDate.parse("2022-07-06"));


        //then
        webTestClient.put()
                     .uri(MOVIES_INFO_URI + "/{id}", movieInfoId)
                     .bodyValue(movieInfo)
                     .exchange()
                     .expectStatus()
                     .isBadRequest()
                     .expectBody(String.class)
                     .consumeWith(stringEntityExchangeResult -> {
                         String responseBody = stringEntityExchangeResult.getResponseBody();
                         System.out.println("responseBody : " + responseBody);
                         String expectedErrorMsg = "movieInfo.cast must be present,movieInfo.name must be present,movieInfo.year must be a Positive value";
                         assert responseBody != null;
                         assertEquals(expectedErrorMsg, responseBody);
                     });
    }

}
