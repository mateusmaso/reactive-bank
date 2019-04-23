# Documentation

## `POST` /accounts

Request

```
{
  "currency": "USD"
}
```

Response

```
{
  "id": "b9317392-7bba-41ed-8a3a-47db82b0a993"
}
```

## `GET` /accounts/:id/balance

Response

```
{
  "amount": 0,
  "currency": "USD"
}
```

## `POST` /load

Request

```
{
  "accountId": "9c62d58a-9d10-4117-9653-7009dfdbfdb9",
  "amount": 2.32,
  "currency": "USD"
}
```

Response

```
{
  "id": "b9dd937c-1da3-40d7-b824-60cb2391b4b3",
  "balance": {
    "amount": 2.32,
    "currency": "USD"
  }
}
```

## `POST` /transfers

Request

```
{
  "accountId": "9c62d58a-9d10-4117-9653-7009dfdbfdb9",
  "amount": 2.32,
  "currency": "USD"
}
```

Response

```
{
  "id": "53b97cc8-94ca-4cd4-8f13-688505ae7336",
  "balance": {
    "amount": 0,
    "currency": "USD"
  }
}
```

## `POST` /transfers

Request

```
{
  "fromAccountId": "9c62d58a-9d10-4117-9653-7009dfdbfdb9",
  "toAccountId": "7649d242-fb1b-4dbb-aa4c-d519360d6a32",
  "amount": 2.32,
  "currency": "USD"
}
```

Response

```
{
  "id": "230a46fc-5152-43da-bb7f-7e638280168f"
}
```

