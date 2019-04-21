package money.account.services;

import java.util.concurrent.CompletableFuture;

import money.MonetaryAmount;
import money.transaction.models.Transaction;

public interface AccountService {
  public CompletableFuture<Transaction> transfer(String fromAccountId, String toAccountId, MonetaryAmount amount);
  public CompletableFuture<Transaction> load(String accountId, MonetaryAmount amount);
  public CompletableFuture<Transaction> unload(String accountId, MonetaryAmount amount);
  public CompletableFuture<MonetaryAmount> getBalance(String accountId);
}