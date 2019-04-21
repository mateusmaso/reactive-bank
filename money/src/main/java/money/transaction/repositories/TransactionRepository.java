package money.transaction.repositories;

import java.util.concurrent.CompletableFuture;

import money.MonetaryAmount;
import money.account.models.Account;
import money.transaction.models.Transaction;

public interface TransactionRepository {
  public CompletableFuture<MonetaryAmount> sumBalance(Account account);
  public CompletableFuture<Transaction> addTransaction(Transaction transaction);
}