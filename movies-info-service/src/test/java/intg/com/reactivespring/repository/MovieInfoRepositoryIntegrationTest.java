package com.reactivespring.repository;

import com.reactivespring.domain.MovieInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataMongoTest
@ActiveProfiles("test")
class MovieInfoRepositoryIntegrationTest {

    @Autowired
    MovieInfoRepository movieInfoRepository;

    @BeforeEach
    void setUp() {
        List<MovieInfo> movieInfos = List.of(new MovieInfo(null, "Batman Begins",
                        2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15")),
                new MovieInfo(null, "The Dark Knight",
                        2008, List.of("Christian Bale", "HeathLedger"), LocalDate.parse("2008-07-18")),
                new MovieInfo("abc", "Dark Knight Rises",
                        2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20")));

        movieInfoRepository.saveAll(movieInfos)
                           .blockLast();
    }

    @AfterEach
    void tearDown() {
        movieInfoRepository.deleteAll()
                           .block();
    }

    @Test
    public void findAll() {
        // given

        // when
        Flux<MovieInfo> moviesInfoFlux = movieInfoRepository.findAll()
                                                            .log();

        // then
        StepVerifier.create(moviesInfoFlux)
                    .expectNextCount(3)
                    .verifyComplete();
    }

    @Test
    public void findById() {
        // given

        // when
        Mono<MovieInfo> moviesInfoMono = movieInfoRepository.findById("abc")
                                                            .log();

        // then
        StepVerifier.create(moviesInfoMono)
                    .assertNext(movieInfo -> {
                        assertEquals("Dark Knight Rises", movieInfo.getName());
                    })
                    .verifyComplete();
    }


    @Test
    public void saveMovieInfo() {
        // given
        MovieInfo movieInfo = new MovieInfo(null, "Thor: Love and Thunder",
                2022, List.of("Christian Bale", "Natalie Portman"), LocalDate.parse("2022-07-06"));
        // when
        Mono<MovieInfo> moviesInfoMono = movieInfoRepository.save(movieInfo)
                                                            .log();

        // then
        StepVerifier.create(moviesInfoMono)
                    .assertNext(movieInfo1 -> {
                        assertNotNull(movieInfo1.getMovieInfoId());
                        assertEquals("Thor: Love and Thunder", movieInfo1.getName());
                    })
                    .verifyComplete();
    }

    @Test
    public void updateMovieInfo() {
        // given
        MovieInfo movieToUpdate = movieInfoRepository.findById("abc")
                                                     .block();
        Objects.requireNonNull(movieToUpdate)
               .setYear(2024);

        // when
        Mono<MovieInfo> moviesInfoMono = movieInfoRepository.save(movieToUpdate)
                                                            .log();

        // then
        StepVerifier.create(moviesInfoMono)
                    .assertNext(movieInfo1 -> {
                        assertEquals(2024, movieInfo1.getYear());
                    })
                    .verifyComplete();
    }


    @Test
    public void deleteMovieInfo() {
        // given

        // when
        movieInfoRepository.deleteById("abc")
                           .block();

        Flux<MovieInfo> moviesInfoFlux = movieInfoRepository.findAll()
                                                            .log();

        // then
        StepVerifier.create(moviesInfoFlux)
                    .expectNextCount(2)
                    .verifyComplete();
    }

}