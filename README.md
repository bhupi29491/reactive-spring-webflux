# reactive-spring-webflux

This repository contains multiple Spring WebFlux-based services that demonstrate reactive programming with Spring Boot
and Reactor. The following services are part of the project:

- **movies-service**: Handles movie information.
- **movies-review-service**: Manages reviews for movies.
- **movies-info-service**: Provides movie-related information.
- **reactive-programming-using-reactor**: A module to demonstrate reactive programming using Project Reactor.

## Project Structure

The root project contains the following sub-modules:

- `movies-service`
- `movies-review-service`
- `movies-info-service`
- `reactive-programming-using-reactor`

### movies-service

This module is responsible for handling movie-related operations.

**Key dependencies:**

- Spring Boot WebFlux (`spring-boot-starter-webflux`)
- Validation (`spring-boot-starter-validation`)
- Lombok for reducing boilerplate code (`lombok`)
- JUnit 5 and Reactor Test for testing (`spring-boot-starter-test`, `reactor-test`)

**Source Sets:**

- `src/test/java/unit`: Unit tests
- `src/test/java/intg`: Integration tests

### movies-review-service

This service handles movie reviews, using MongoDB as a reactive data source.

**Key dependencies:**

- Spring Boot WebFlux (`spring-boot-starter-webflux`)
- Spring Boot MongoDB Reactive (`spring-boot-starter-data-mongodb-reactive`)
- Validation (`spring-boot-starter-validation`)
- Lombok for reducing boilerplate code (`lombok`)
- Embedded MongoDB for testing (`de.flapdoodle.embed.mongo`)
- JUnit 5 and Reactor Test for testing (`spring-boot-starter-test`, `reactor-test`)

**Source Sets:**

- `src/test/java/unit`: Unit tests
- `src/test/java/intg`: Integration tests

### movies-info-service

The `movies-info-service` manages movie information, using MongoDB as the database and exposes a reactive API.

**Key dependencies:**

- Spring Boot WebFlux (`spring-boot-starter-webflux`)
- Spring Boot MongoDB Reactive (`spring-boot-starter-data-mongodb-reactive`)
- Validation (`spring-boot-starter-validation`)
- Lombok for reducing boilerplate code (`lombok`)
- Embedded MongoDB for testing (`de.flapdoodle.embed.mongo`)
- JUnit 5 and Reactor Test for testing (`spring-boot-starter-test`, `reactor-test`)

**Source Sets:**

- `src/test/java/unit`: Unit tests
- `src/test/java/intg`: Integration tests

## Getting Started

### Prerequisites

- Java 11 or higher
- Gradle
- MongoDB (for `movies-info-service` and `movies-review-service`)

### Running the Services

Each service can be started independently using:

```bash
./gradlew :<service-name>:bootRun
```

For example, to run the `movies-service`:

```bash
./gradlew :movies-service:bootRun
```

### Running Tests

To run the tests for any service, use:

```bash
./gradlew :<service-name>:test
```

For example, to run the tests for `movies-review-service`:

```bash
./gradlew :movies-review-service:test
```