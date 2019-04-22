# reactive-bank

- Java 8.
- Reactive Framework. (Vert.x? Akka?)
- Dependency Injection. (Manual)
- Event-Driven. (Event Bus)
- FP + Immutability vs OOP + Akka.
- Mem atomicity?
- TDD using Spock? (Unit + Integration + E2E for Parallelism)
- Dockerize + executable as a standalone program
- Abstractions (Account, Transfer, Balance)
- Business Rules (cannot be negative, must have balance)
- Receive transfer, debit, send event async (overengineer?).

## API Spec

- POST /transfers

> Request

```
{
  "fromAccountId": "111",
  "toAccountId": "222",
  "amount": 2.32,
  "currency": "USD"
}
```

> Response

```
{
  "id": "b9317392-7bba-41ed-8a3a-47db82b0a993"
}
```