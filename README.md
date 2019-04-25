# reactive-bank

Keep in mind to follow these principles before contribute:

- Try to solve the problems with a modern approach.
- Java (11 or higher) or any programming language equivalent. (eg: Scala, Elixir)
- Use architecture to protect the business rules from the technology layer. (eg: hexagonal/clean/onion)
- Every domain should be a module using the [microservices principles](https://martinfowler.com/articles/microservices.html).
- Think how to solve the problems asynchronously without locking.
- Use the principles of reactive programming such as:
  - Message-driven.
  - Responsive.
  - Elastic.
  - Resilient.
- Easy setup using `docker-compose up` in the root project to run all microservices.

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
