package money.inmemory;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import money.MonetaryAmount;
import money.account.models.Account;
import money.transaction.models.Transaction;
import money.transaction.models.TransactionEntry;
import money.transaction.repositories.TransactionRepository;

public class InMemoryTransactionRepository implements TransactionRepository {
  private final TransactionEntriesByAccountStore transactionEntriesByAccountStore;

  public InMemoryTransactionRepository(TransactionEntriesByAccountStore transactionEntriesByAccountStore) {
    this.transactionEntriesByAccountStore = transactionEntriesByAccountStore;
  }

  @Override
  public CompletableFuture<MonetaryAmount> sumBalance(Account account) {
    return CompletableFuture.supplyAsync(() -> {
      return transactionEntriesByAccountStore
        .get(account)
        .map((transactionEntries) -> {
          return transactionEntries
            .stream()
            .map(TransactionEntry::getSignedAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        })
        .map((balance) -> new MonetaryAmount(account.getCurrency(), balance))
        .orElse(new MonetaryAmount(account.getCurrency(), BigDecimal.ZERO));
    });
  }

  @Override
  public CompletableFuture<Transaction> addTransaction(Transaction transaction) {
    return CompletableFuture.supplyAsync(() -> {
      Account debitAccount = transaction.getDebitEntry().getAccount();
      Account creditAccount = transaction.getCreditEntry().getAccount();

      List<TransactionEntry> debitAccountTransactions = transactionEntriesByAccountStore.get(debitAccount).get();
      List<TransactionEntry> creditAccountTransactions = transactionEntriesByAccountStore.get(creditAccount).get();

      transactionEntriesByAccountStore.put(debitAccount, debitAccountTransactions);
      transactionEntriesByAccountStore.put(creditAccount, creditAccountTransactions);

      return transaction;
    });
  }
}