# reactive-bank

This is a modern implementation of a banking API based on the [reactive manifesto](https://www.reactivemanifesto.org) and [microservices architecture](https://martinfowler.com/articles/microservices.html). Currently, these are the following services available:

- [Money](/money): Holds balance and transactions of the general ledger.

## [API](API.md)

| Method | Resource                                 | Description                                         |
| ------ | ---------------------------------------- | --------------------------------------------------- |
| POST   | /accounts                                | Creates an account                                  |
| GET    | /accounts/:id/balance                    | Retrieves the account balance                       |
| POST   | /transfers                               | Transfer money between accounts                     |
| POST   | /load                                    | Load money into the account                         |
| POST   | /unload                                  | Unload money from the account                       |

## Running

```sh
$ docker-compose up
```

## Contributing

Keep in mind to follow these principles before contributing:

- Try to solve the problems with a modern approach.
- Choose any programming language although JVM is preferred. (eg: Java, Scala, Clojure)
- Protect the business rules from the technology layer. (eg: hexagonal/clean/onion)
- Every domain should be a module using the [microservices principles](https://martinfowler.com/articles/microservices.html).
- Think how to solve the problems asynchronously.
- Use the principles of reactive programming such as:
  - Message-driven.
  - Responsive.
  - Elastic.
  - Resilient.
- Easy setup in the root project that runs all microservices.