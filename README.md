# reactive-bank

## Pre-requisites

Keep in mind to follow these principles before contribute:

- Try to solve the problems with a modern approach
- Modern Java version (11 or higher) or any modern programming language equivalent (eg: Scala, Elixir)
- Using architecture to protect the business rules from the technology layer (eg: hexagonal, clean, onion or another one architecture that you feel comfortable to use)
- Every domain should be a module using the microservices principles
- Think how to solve the problems asynchronously (No locking approach please)
- Using the principles of reactive programming
  - Message-driven
  - Responsive
  - Elastic
  - Resilient (Crash happens. So you must think about how you can deal with failures using an approach such as DLQ or compensation transaction).

  
## Description  

This is a proposal of a bank API written using reactive programming and concurrent operations in memory. It exposes a web interface to the external world and handles multiple requests concurrently. Bear in mind that this project was not meant to be ran in a distributed environment since it's data is kept locally without persistance after killing the process.

- Used an in-memory data store. (will not work in distributed systems)
- Chose atomic data models with append-only structures to avoid dealing with `ACID` transactions.
- Wrote in Java 8 using `Streams` and immutable objects for composability and thread safety.
- Chose to use async request handling with `Javalin` and `ComputableFuture` to allow more web concurrency.
- Designed based on onion/hexagonal architecture. (core domain and infra classes are separeted)
- Followed `SOLID` principles such as dependency injection and inversion.
- Handled debit account operations with a single Java `synchronized` for locking to avoid deadlocks.
- Used `TDD` with `JUnit` and `Mockito` for unit and integration tests.

## [API](API.md)

| Method | Resource                                 | Description                                         |
| ------ | ---------------------------------------- | --------------------------------------------------- |
| POST   | /accounts                                | Creates an account                                  |
| GET    | /accounts/:id/balance                    | Retrieves the account balance                       |
| POST   | /transfers                               | Transfer money between accounts                     |
| POST   | /load                                    | Load money into the account                         |
| POST   | /unload                                  | Unload money from the account                       |

## Installing

```sh
$ ./gradlew build
```

## Running API

```sh
$ ./gradlew run
```

> Default port `8080`

## Testing

```sh
$ ./gradlew test
```
