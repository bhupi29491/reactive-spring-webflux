package com.reactivespring.client;

import com.reactivespring.domain.MovieInfo;
import com.reactivespring.exception.MoviesInfoClientException;
import com.reactivespring.exception.MoviesInfoServerException;
import com.reactivespring.util.RetryUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class MoviesInfoRestClient {

    private WebClient webClient;

    @Value("${restClient.moviesInfoUrl}")
    private String moviesInfoUrl;

    public MoviesInfoRestClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<MovieInfo> fetchMovieInfo(String movieId) {

        String url = moviesInfoUrl.concat("/{id}");
        return webClient.get()
                        .uri(url, movieId)
                        .retrieve()
                        .onStatus(HttpStatus::is4xxClientError, clientResponse -> {
                            log.info("Status code is : {}",
                                     clientResponse.statusCode()
                                                   .value());
                            if (clientResponse.statusCode()
                                              .equals(HttpStatus.NOT_FOUND)) {
                                return Mono.error(new MoviesInfoClientException(
                                        "There is no movieInfo available for the passed in Id : " + movieId,
                                        clientResponse.statusCode()
                                                      .value()));
                            }
                            return clientResponse.bodyToMono(String.class)
                                                 .flatMap(responseMessage -> Mono.error(new MoviesInfoClientException(
                                                         responseMessage,
                                                         clientResponse.statusCode()
                                                                       .value())));
                        })
                        .onStatus(HttpStatus::is5xxServerError, clientResponse -> {
                            log.info("Status code is : {}",
                                     clientResponse.statusCode()
                                                   .value());
                            return clientResponse.bodyToMono(String.class)
                                                 .flatMap(responseMessage -> Mono.error(new MoviesInfoServerException(
                                                         "Server Exception in MoviesInfoService " + responseMessage)));
                        })
                        .bodyToMono(MovieInfo.class)
                        .retryWhen(RetryUtil.retrySpec())
                        .log();
    }

    public Flux<MovieInfo> fetchMovieInfoStream() {

        String url = moviesInfoUrl.concat("/stream");

        return webClient.get()
                        .uri(url)
                        .retrieve()
                        .onStatus(HttpStatus::is4xxClientError, clientResponse -> {
                            log.info("Status code is : {}",
                                     clientResponse.statusCode()
                                                   .value());

                            return clientResponse.bodyToMono(String.class)
                                                 .flatMap(responseMessage -> Mono.error(new MoviesInfoClientException(
                                                         responseMessage,
                                                         clientResponse.statusCode()
                                                                       .value())));
                        })
                        .onStatus(HttpStatus::is5xxServerError, clientResponse -> {
                            log.info("Status code is : {}",
                                     clientResponse.statusCode()
                                                   .value());
                            return clientResponse.bodyToMono(String.class)
                                                 .flatMap(responseMessage -> Mono.error(new MoviesInfoServerException(
                                                         "Server Exception in MoviesInfoService " + responseMessage)));
                        })
                        .bodyToFlux(MovieInfo.class)
                        .retryWhen(RetryUtil.retrySpec())
                        .log();
    }
}
