# reactive-bank

- Written in Java 8.
- Reactive programming using `ComputableFuture`.
- Reactive web server with `Javalin`.
- Manual dependency injection.
- Append-only & atomic data model. (`Transaction`)
- TDD using `JUnit` and `Mockito`.

| Method | Resource                                 | Description                                         |
| ------ | ---------------------------------------- | --------------------------------------------------- |
| POST   | /accounts                                | Creates an account                                  |
| GET    | /accounts/:id/balance                    | Retrieves account's balance                         |
| POST   | /transfers                               | Transfer money between accounts                     |
| POST   | /load                                    | Load money into account                             |
| POST   | /unload                                  | Unload money from account                           |

## Install

```sh
$ ./gradlew build
```

## Run API

```sh
$ ./gradlew run
```

## Testing

```sh
$ ./gradlew test
```