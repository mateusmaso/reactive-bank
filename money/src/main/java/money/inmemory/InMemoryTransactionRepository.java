package money.inmemory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import money.MonetaryAmount;
import money.account.Account;
import money.transaction.Transaction;
import money.transaction.TransactionEntry;
import money.transaction.TransactionRepository;

public class InMemoryTransactionRepository implements TransactionRepository {
  private final InMemoryStore store;

  public InMemoryTransactionRepository(InMemoryStore store) {
    this.store = store;
  }

  @Override
  public CompletableFuture<MonetaryAmount> sumBalance(Account account) {
    BigDecimal balance = Optional.ofNullable(this.store
      .getTransactionEntriesByAccountId()
      .get(account.getId())).orElseGet(() -> new ArrayList<>())
      .stream()
      .map(TransactionEntry::getSignedAmount)
      .reduce(BigDecimal.ZERO, BigDecimal::add);

    return CompletableFuture.completedFuture(
      new MonetaryAmount(account.getCurrency(), balance)
    );
  }

  @Override
  public CompletableFuture<Transaction> create(Transaction transaction) {
    return CompletableFuture.completedFuture(
      this.store.putTransaction(transaction)
    );
  }
}