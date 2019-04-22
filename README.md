# reactive-bank

- Written in Java 8 using `Streams` and final objects.
- Reactive programming with `ComputableFuture` in I/O blocking methods.
- Async request handling with `Javalin`.
- Dependency injection and inversion written manually.
- In-memory datastore.
- Concurrency handled through Java's synchronization.
- Data models with append-only and `ACID` structures (`Account`, `Transaction`)
- `TDD` using `JUnit` and `Mockito`.

| Method | Resource                                 | Description                                         |
| ------ | ---------------------------------------- | --------------------------------------------------- |
| POST   | /accounts                                | Creates an account                                  |
| GET    | /accounts/:id/balance                    | Retrieves the account balance                       |
| POST   | /transfers                               | Transfer money between accounts                     |
| POST   | /load                                    | Load money into the account                         |
| POST   | /unload                                  | Unload money from the account                        |

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