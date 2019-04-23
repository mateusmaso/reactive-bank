package money.transaction;

import java.util.concurrent.CompletableFuture;

import money.MonetaryAmount;
import money.account.Account;
import money.transaction.Transaction;

public interface TransactionRepository {
  public CompletableFuture<MonetaryAmount> sumBalance(Account account);
  public CompletableFuture<Transaction> create(Transaction transaction);
}