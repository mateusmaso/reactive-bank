package money.transaction.repositories;

import java.math.BigDecimal;
import java.util.List;

import money.MonetaryAmount;
import money.account.models.Account;
import money.store.TransactionEntriesByAccountStore;
import money.transaction.models.Transaction;
import money.transaction.models.TransactionEntry;

public class TransactionRepositoryImpl implements TransactionRepository {
  private final TransactionEntriesByAccountStore transactionEntriesByAccountStore;

  public TransactionRepositoryImpl(TransactionEntriesByAccountStore transactionEntriesByAccountStore) {
    this.transactionEntriesByAccountStore = transactionEntriesByAccountStore;
  }

  @Override
  public MonetaryAmount sumBalance(Account account) {
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
  }

  @Override
  public Transaction addTransaction(Transaction transaction) {
    Account debitAccount = transaction.getDebitEntry().getAccount();
    Account creditAccount = transaction.getCreditEntry().getAccount();

    List<TransactionEntry> debitAccountTransactions = transactionEntriesByAccountStore.get(debitAccount).get();
    List<TransactionEntry> creditAccountTransactions = transactionEntriesByAccountStore.get(creditAccount).get();

    transactionEntriesByAccountStore.put(debitAccount, debitAccountTransactions);
    transactionEntriesByAccountStore.put(creditAccount, creditAccountTransactions);

    return transaction;
  }
}