# reactive-bank

This is a proposal of a bank API written using reactive programming and ACID concurrent operations in memory. It exposes a web interface to the external world and it handle multiple requests concurrently. Bear in mind that this project was not meant to be ran in a distributed environment since it's data is kept locally without persistance guarantee after the process is killed.

- Written in Java 8 using `Streams` and final objects for immutability.
- Reactive programming with `ComputableFuture` in I/O blocking methods.
- Async request handling with `Javalin` and `ComputableFuture`.
- Dependency injection and inversion written manually.
- In-memory datastore.
- Concurrency handled through Java `synchronized`.
- Data models with append-only and `ACID` structures (`Account`, `Transaction`)
- `TDD` using `JUnit` and `Mockito` for unit, integration and concurrency tests.

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