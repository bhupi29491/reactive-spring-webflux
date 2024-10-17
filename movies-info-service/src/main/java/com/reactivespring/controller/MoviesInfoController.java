package com.reactivespring.controller;

import com.reactivespring.domain.MovieInfo;
import com.reactivespring.service.MovieInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import javax.validation.Valid;

@RestController
@RequestMapping("/v1")
@Slf4j
public class MoviesInfoController {

    Sinks.Many<MovieInfo> moviesInfoSink = Sinks.many()
                                                .replay()
                                                .latest();

    private MovieInfoService movieInfoService;


    public MoviesInfoController(MovieInfoService movieInfoService) {
        this.movieInfoService = movieInfoService;
    }

    @GetMapping("/movieinfos")
    public Flux<MovieInfo> getAllMovieInfos(@RequestParam(value = "year", required = false) Integer year, @RequestParam(value = "name", required = false) String movieName) {
        log.info("Year  : {} & MovieName : {} ", year, movieName);

        if (year != null) {
            return movieInfoService.getMovieInfoByYear(year);
        }
        if (movieName != null) {
            return movieInfoService.getMovieInfoByName(movieName);
        }
        return movieInfoService.getAllMovieInfos()
                               .log();
    }

    @GetMapping("/movieinfos/{id}")
    public Mono<ResponseEntity<MovieInfo>> getMovieInfoById(@PathVariable String id) {
        return movieInfoService.getMovieInfoById(id)
                               .map(ResponseEntity.ok()::body)
                               .switchIfEmpty(Mono.just(ResponseEntity.notFound()
                                                                      .build()))
                               .log();
    }

    @GetMapping(value = "/movieinfos/stream", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<MovieInfo> getMovieInfoById() {
        return moviesInfoSink.asFlux()
                             .log();
    }

    @PostMapping("/movieinfos")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<MovieInfo> addMovieInfo(@RequestBody @Valid MovieInfo movieInfo) {

        return movieInfoService.addMovieInfo(movieInfo)
                               .doOnNext(savedInfo -> moviesInfoSink.tryEmitNext(savedInfo));

    }


    @PutMapping("/movieinfos/{id}")
    public Mono<ResponseEntity<MovieInfo>> updateMovieInfo(@RequestBody @Valid MovieInfo updatedMovieInfo, @PathVariable String id) {
        return movieInfoService.updateMovieInfo(updatedMovieInfo, id)
                               .map(ResponseEntity.ok()::body)
                               .switchIfEmpty(Mono.just(ResponseEntity.notFound()
                                                                      .build()))
                               .log();
    }

    @DeleteMapping("/movieinfos/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteMovieInfo(@PathVariable String id) {
        return movieInfoService.deleteMovieInfo(id)
                               .log();
    }


}
