# reactive-bank

Keep in mind to follow these principles before contribute:

- Try to solve the problems with a modern approach
- Modern Java version (11 or higher) or any modern programming language equivalent (eg: Scala, Elixir)
- Using architecture to protect the business rules from the technology layer (eg: hexagonal, clean, onion or another one architecture that you feel comfortable to use)
- Every domain should be a module using the [microservices principles](https://martinfowler.com/articles/microservices.html)
- Think how to solve the problems asynchronously (No locking approach please)
- Using the principles of reactive programming
  - Message-driven
  - Responsive
  - Elastic
  - Resilient (Crash happens. So you must think about how you can deal with failures using an approach such as DLQ or compensation transaction).
- Just execute `docker-compose up` in the root project to running all microservices modules

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
