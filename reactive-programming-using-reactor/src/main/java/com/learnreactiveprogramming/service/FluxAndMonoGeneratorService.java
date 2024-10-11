package com.learnreactiveprogramming.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

public class FluxAndMonoGeneratorService {

    public static void main(String[] args) {

        FluxAndMonoGeneratorService fluxAndMonoGeneratorService = new FluxAndMonoGeneratorService();
        fluxAndMonoGeneratorService.namesFlux()
                                   .subscribe(name -> {
                                       System.out.println("Name is : " + name);
                                   });

        fluxAndMonoGeneratorService.nameMono()
                                   .subscribe(name -> {
                                       System.out.println("Mono name is : " + name);
                                   });
    }

    public Flux<String> namesFlux() {
        return Flux.fromIterable(List.of("Bhupi", "Viru", "Shiva"))
                   .log(); // Db or a remote service call
    }

    public Mono<String> nameMono() {
        return Mono.just("Vivek")
                   .log();
    }

    public Flux<String> namesFlux_map(int stringLength) {
        // filter the string whose length is greater than 4
        return Flux.fromIterable(List.of("Bhupi", "Viru", "Shiva"))
                   .map(String::toUpperCase)
                   .filter(s -> s.length() > stringLength)
                   .map(s -> s.length() + "-" + s)
                   .log(); // Db or a remote service call
    }

    public Flux<String> namesFlux_Immutability() {
        var namesFlux = Flux.fromIterable(List.of("Bhupi", "Viru", "Shiva"));
        namesFlux.map(String::toUpperCase);
        return namesFlux;
    }

    public Flux<String> namesFlux_flatmap(int stringLength) {
        // filter the string whose length is greater than 4
        return Flux.fromIterable(List.of("Bhupi", "Viru", "Shiva"))
                   .map(String::toUpperCase)
                   .filter(s -> s.length() > stringLength)
                   // BHUPI, SHIVA -> B, H, U, P, I, S,H, I, V, A
                   .flatMap(s -> splitString(s))
                   .log(); // Db or a remote service call
    }

    // BHUPI -> Flux(B,H,U,P,I)
    public Flux<String> splitString(String name) {
        var charArray = name.split("");
        return Flux.fromArray(charArray);
    }

    public Flux<String> namesFlux_flatmap_async(int stringLength) {
        // filter the string whose length is greater than 4
        return Flux.fromIterable(List.of("Bhupi", "Viru", "Shiva"))
                   .map(String::toUpperCase)
                   .filter(s -> s.length() > stringLength)
                   // BHUPI, SHIVA -> B, H, U, P, I, S,H, I, V, A
                   .flatMap(s -> splitString_withDelay(s))
                   .log(); // Db or a remote service call
    }

    // BHUPI -> Flux(B,H,U,P,I)
    public Flux<String> splitString_withDelay(String name) {
        var charArray = name.split("");
        var delay = new Random().nextInt(1000);
        return Flux.fromArray(charArray)
                   .delayElements(Duration.ofMillis(delay));
    }

    public Flux<String> namesFlux_concatMap(int stringLength) {
        // filter the string whose length is greater than 4
        return Flux.fromIterable(List.of("Bhupi", "Viru", "Shiva"))
                   .map(String::toUpperCase)
                   .filter(s -> s.length() > stringLength)
                   // BHUPI, SHIVA -> B, H, U, P, I, S,H, I, V, A
                   .concatMap(s -> splitString_withDelay(s))
                   .log(); // Db or a remote service call
    }

    public Mono<String> nameMono_map_filter(int stringLength) {
        return Mono.just("Vivek")
                   .map(String::toUpperCase)
                   .filter(s -> s.length() > stringLength);
    }

    public Mono<List<String>> nameMono_flatMap(int stringLength) {
        return Mono.just("Vivek")
                   .map(String::toUpperCase)
                   .filter(s -> s.length() > stringLength)
                   .flatMap(this::splitStringMono);  // Mono<List of V, I, V, E, K>
    }

    public Flux<String> namesMono_flatMapMany(int stringLength) {
        return Mono.just("Vivek")
                   .map(String::toUpperCase)
                   .filter(s -> s.length() > stringLength)
                   .flatMapMany(this::splitString)
                   .log();  // Mono<List of V, I, V, E, K>
    }

    public Flux<String> namesFlux_transform(int stringLength) {
        // filter the string whose length is greater than 4

        Function<Flux<String>, Flux<String>> filterMap = name -> name.map(String::toUpperCase)
                                                                     .filter(s -> s.length() > stringLength);

        return Flux.fromIterable(List.of("Bhupi", "Viru", "Shiva"))
                   .transform(filterMap)
                   // BHUPI, SHIVA -> B, H, U, P, I, S,H, I, V, A
                   .flatMap(s -> splitString(s))
                   .defaultIfEmpty("default")
                   .log(); // Db or a remote service call
    }

    public Flux<String> namesFlux_transform_switchIfEmpty(int stringLength) {
        // filter the string whose length is greater than 4

        Function<Flux<String>, Flux<String>> filterMap = name ->
                name.map(String::toUpperCase)
                    .filter(s -> s.length() > stringLength)
                    .flatMap(s -> splitString(s));

        Flux<String> defaultFlux = Flux.just("default")
                                       .transform(filterMap);// "D", "E", "F", "A", "U", "L", "T"

        return Flux.fromIterable(List.of("Bhupi", "Viru", "Shiva"))
                   .transform(filterMap)
                   // BHUPI, SHIVA -> B, H, U, P, I, S,H, I, V, A

                   .switchIfEmpty(defaultFlux)
                   .log(); // Db or a remote service call
    }

    private Mono<List<String>> splitStringMono(String s) {
        var charArray = s.split("");
        List<String> charList = List.of(charArray); // VIVEK -> V, I, V, E, K
        return Mono.just(charList);
    }

    public Flux<String> explore_concat() {

        var abcFlux = Flux.just("A", "B", "C");

        var defFlux = Flux.just("D", "E", "F");

        return Flux.concat(abcFlux, defFlux)
                   .log();
    }

    public Flux<String> explore_concatWith() {

        var abcFlux = Flux.just("A", "B", "C");

        var defFlux = Flux.just("D", "E", "F");

        return abcFlux.concatWith(defFlux)
                      .log();
    }

    public Flux<String> explore_concatWith_Mono() {

        var aMono = Mono.just("A");

        var bMono = Mono.just("B");

        return aMono.concatWith(bMono)
                    .log(); // A, B
    }

    public Flux<String> explore_Merge() {

        var abcFlux = Flux.just("A", "B", "C")
                          .delayElements(Duration.ofMillis(100));

        var defFlux = Flux.just("D", "E", "F")
                          .delayElements(Duration.ofMillis(125));

        return Flux.merge(abcFlux, defFlux)
                   .log();
    }

    public Flux<String> explore_MergeWith() {

        var abcFlux = Flux.just("A", "B", "C")
                          .delayElements(Duration.ofMillis(100));

        var defFlux = Flux.just("D", "E", "F")
                          .delayElements(Duration.ofMillis(125));

        return abcFlux.mergeWith(defFlux)
                      .log();
    }

    public Flux<String> explore_MergeWith_Mono() {

        var aMono = Mono.just("A");

        var bMono = Mono.just("B");

        return aMono.mergeWith(bMono)
                    .log();
    }

    public Flux<String> explore_MergeSequential() {

        var abcFlux = Flux.just("A", "B", "C")
                          .delayElements(Duration.ofMillis(100));

        var defFlux = Flux.just("D", "E", "F")
                          .delayElements(Duration.ofMillis(125));

        return Flux.mergeSequential(abcFlux, defFlux)
                   .log();
    }

    public Flux<String> explore_Zip() {

        var abcFlux = Flux.just("A", "B", "C");

        var defFlux = Flux.just("D", "E", "F");

        return Flux.zip(abcFlux, defFlux, (first, second) -> first + second)
                   .log(); // AD BE, CF
    }

    public Flux<String> explore_Zip_1() {

        var abcFlux = Flux.just("A", "B", "C");

        var defFlux = Flux.just("D", "E", "F");

        var _123Flux = Flux.just("1", "2", "3");

        var _456Flux = Flux.just("4", "5", "6");

        return Flux.zip(abcFlux, defFlux, _123Flux, _456Flux)
                   .map(t4 -> t4.getT1() + t4.getT2() + t4.getT3() + t4.getT4())
                   .log(); // AD14 BE25, CF36
    }

    public Flux<String> explore_ZipWith() {

        var abcFlux = Flux.just("A", "B", "C");

        var defFlux = Flux.just("D", "E", "F");

        return abcFlux.zipWith(defFlux, (first, second) -> first + second)
                      .log(); // AD BE, CF
    }

    public Mono<String> explore_MergeZipWith_Mono() {

        var aMono = Mono.just("A"); //A

        var bMono = Mono.just("B"); //B

        return aMono.zipWith(bMono)
                    .map(t2 -> t2.getT1() + t2.getT2()) //AB
                    .log();  // AB
    }

}
