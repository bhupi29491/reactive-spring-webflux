package com.reactivespring.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

@WebFluxTest(controllers = FluxAndMonoController.class)
@AutoConfigureWebTestClient
class FluxAndMonoControllerTest {

    @Autowired
    WebTestClient webTestClient;

    @Test
    public void fluxTest() {

        webTestClient.get()
                     .uri("/flux")
                     .exchange()
                     .expectStatus()
                     .is2xxSuccessful()
                     .expectBodyList(Integer.class)
                     .hasSize(3);

    }

    @Test
    public void fluxTest_Approach2() {

        Flux<Integer> integerFlux = webTestClient.get()
                                                 .uri("/flux")
                                                 .exchange()
                                                 .expectStatus()
                                                 .is2xxSuccessful()
                                                 .returnResult(Integer.class)
                                                 .getResponseBody();

        StepVerifier.create(integerFlux)
                    .expectNext(1, 2, 3)
                    .verifyComplete();

    }

    @Test
    public void fluxTest_Approach3() {

        webTestClient.get()
                     .uri("/flux")
                     .exchange()
                     .expectStatus()
                     .is2xxSuccessful()
                     .expectBodyList(Integer.class)
                     .consumeWith(listEntityExchangeResult -> {
                         List<Integer> responseBody = listEntityExchangeResult.getResponseBody();
                         assert (Objects.requireNonNull(responseBody)
                                        .size() == 3);
                     });

    }

    @Test
    public void monoTest() {

        webTestClient.get()
                     .uri("/mono")
                     .exchange()
                     .expectStatus()
                     .is2xxSuccessful()
                     .expectBody(String.class)
                     .consumeWith(stringEntityExchangeResult -> {
                         String responseBody = stringEntityExchangeResult.getResponseBody();
                         assertEquals("hello-world", responseBody);

                     });
    }

    @Test
    public void streamTest() {

        Flux<Long> integerFlux = webTestClient.get()
                                              .uri("/stream")
                                              .exchange()
                                              .expectStatus()
                                              .is2xxSuccessful()
                                              .returnResult(Long.class)
                                              .getResponseBody();

        StepVerifier.create(integerFlux)
                    .expectNext(0L, 1L, 2L, 3L)
                    .thenCancel()
                    .verify();

    }

}