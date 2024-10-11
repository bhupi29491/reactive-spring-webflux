package com.learnreactiveprogramming.service;

import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.util.List;


class FluxAndMonoGeneratorServiceTest {

    FluxAndMonoGeneratorService fluxAndMonoGeneratorService = new FluxAndMonoGeneratorService();

    @Test
    void namesFlux() {

        // when
        var namesFlux = fluxAndMonoGeneratorService.namesFlux();

        //then
        StepVerifier.create(namesFlux)
//                .expectNext("Bhupi", "Viru", "Shiva")
                    .expectNextCount(3)
                    .verifyComplete();

    }

    @Test
    void nameMono() {
    }

    @Test
    void namesFlux_map() {

        // given
        int stringLength = 4;

        // when
        var namesFlux = fluxAndMonoGeneratorService.namesFlux_map(stringLength);

        // then
        StepVerifier.create(namesFlux)
                    .expectNext("5-BHUPI", "5-SHIVA")
                    .verifyComplete();
    }

    @Test
    void namesFlux_Immutability() {

        // when
        var namesFlux = fluxAndMonoGeneratorService.namesFlux_Immutability();

        // then
        StepVerifier.create(namesFlux)
                    .expectNext("Bhupi", "Viru", "Shiva")
                    .verifyComplete();
    }

    @Test
    void namesFlux_flatmap() {
        // given
        int stringLength = 4;

        // when
        var namesFlux = fluxAndMonoGeneratorService.namesFlux_flatmap(stringLength);

        // then
        StepVerifier.create(namesFlux)
                    .expectNext("B", "H", "U", "P", "I", "S", "H", "I", "V", "A")
                    .verifyComplete();
    }

    @Test
    void namesFlux_flatmap_async() {
        // given
        int stringLength = 4;

        // when
        var namesFlux = fluxAndMonoGeneratorService.namesFlux_flatmap_async(stringLength);

        // then
        StepVerifier.create(namesFlux)
//                    .expectNext("B", "H", "U", "P", "I", "S", "H", "I", "V", "A")
                    .expectNextCount(10)
                    .verifyComplete();
    }

    @Test
    void namesFlux_concatMap() {
        // given
        int stringLength = 4;

        // when
        var namesFlux = fluxAndMonoGeneratorService.namesFlux_concatMap(stringLength);

        // then
        StepVerifier.create(namesFlux)
                    .expectNext("B", "H", "U", "P", "I", "S", "H", "I", "V", "A")
//                    .expectNextCount(10)
                    .verifyComplete();
    }

    @Test
    void nameMono_flatMap() {
        // given
        int stringLength = 4;

        // when
        var value = fluxAndMonoGeneratorService.nameMono_flatMap(stringLength);

        // then
        StepVerifier.create(value)
                    .expectNext(List.of("V", "I", "V", "E", "K"))
                    .verifyComplete();
    }

    @Test
    void namesMono_flatMapMany() {
        // given
        int stringLength = 4;

        // when
        var value = fluxAndMonoGeneratorService.namesMono_flatMapMany(stringLength);

        // then
        StepVerifier.create(value)
                    .expectNext("V", "I", "V", "E", "K")
                    .verifyComplete();
    }

    @Test
    void namesFlux_transform() {
        // given
        int stringLength = 4;

        // when
        var namesFlux = fluxAndMonoGeneratorService.namesFlux_transform(stringLength);

        // then
        StepVerifier.create(namesFlux)
                    .expectNext("B", "H", "U", "P", "I", "S", "H", "I", "V", "A")
                    .verifyComplete();
    }

    @Test
    void namesFlux_transform_switchIfEmpty() {
        // given
        int stringLength = 6;

        // when
        var namesFlux = fluxAndMonoGeneratorService.namesFlux_transform_switchIfEmpty(stringLength);

        // then
        StepVerifier.create(namesFlux)
                    .expectNext("D", "E", "F", "A", "U", "L", "T")
                    .verifyComplete();
    }

    @Test
    void explore_concat() {

        //given

        //when
        var concatFlux = fluxAndMonoGeneratorService.explore_concat();

        //then
        StepVerifier.create(concatFlux)
                    .expectNext("A", "B", "C", "D", "E", "F")
                    .verifyComplete();

    }

    @Test
    void explore_concatWith() {

        //given

        //when
        var concatFlux = fluxAndMonoGeneratorService.explore_concatWith();

        //then
        StepVerifier.create(concatFlux)
                    .expectNext("A", "B", "C", "D", "E", "F")
                    .verifyComplete();
    }

    @Test
    void explore_concatWith_Mono() {

        //given

        //when
        var concatFlux = fluxAndMonoGeneratorService.explore_concatWith_Mono();

        //then
        StepVerifier.create(concatFlux)
                    .expectNext("A", "B")
                    .verifyComplete();
    }

    @Test
    void explore_Merge() {

        //given

        //when
        var value = fluxAndMonoGeneratorService.explore_Merge();

        //then
        StepVerifier.create(value)
                    .expectNext("A", "D", "B", "E", "C", "F")
                    .verifyComplete();
    }

    @Test
    void explore_MergeWith() {

        //given

        //when
        var value = fluxAndMonoGeneratorService.explore_MergeWith();

        //then
        StepVerifier.create(value)
                    .expectNext("A", "D", "B", "E", "C", "F")
                    .verifyComplete();
    }

    @Test
    void explore_MergeWith_Mono() {

        //given

        //when
        var value = fluxAndMonoGeneratorService.explore_MergeWith_Mono();

        //then
        StepVerifier.create(value)
                    .expectNext("A", "B")
                    .verifyComplete();
    }

    @Test
    void explore_MergeSequential() {

        //given

        //when
        var value = fluxAndMonoGeneratorService.explore_MergeSequential();

        //then
        StepVerifier.create(value)
                    .expectNext("A", "B", "C", "D", "E", "F")
                    .verifyComplete();
    }

    @Test
    void explore_Zip() {

        //given

        //when
        var value = fluxAndMonoGeneratorService.explore_Zip();

        //then
        StepVerifier.create(value)
                    .expectNext("AD", "BE", "CF")
                    .verifyComplete();
    }

    @Test
    void explore_Zip_1() {

        //given

        //when
        var value = fluxAndMonoGeneratorService.explore_Zip_1();

        //then
        StepVerifier.create(value)
                    .expectNext("AD14", "BE25", "CF36")
                    .verifyComplete();
    }

    @Test
    void explore_ZipWith() {

        //given

        //when
        var value = fluxAndMonoGeneratorService.explore_ZipWith();

        //then
        StepVerifier.create(value)
                    .expectNext("AD", "BE", "CF")
                    .verifyComplete();
    }

    @Test
    void explore_MergeZipWith_Mono() {

        //given

        //when
        var value = fluxAndMonoGeneratorService.explore_MergeZipWith_Mono();

        //then
        StepVerifier.create(value)
                    .expectNext("AB")
                    .verifyComplete();
    }
}