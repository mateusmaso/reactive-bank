package money.inmemory;

import money.account.models.Account;
import money.transaction.models.Transaction;
import money.transaction.models.TransactionEntry;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import java.util.Currency;
import java.util.stream.Collectors;

public class InMemoryStore {
  private AppendOnlyWaitFreeList<Account> accounts;
  private AppendOnlyWaitFreeList<Transaction> transactions;

  public InMemoryStore() {
    this.accounts = new AppendOnlyWaitFreeList<>();
    this.transactions = new AppendOnlyWaitFreeList<>();
    this.putAccount(new Account(Account.OPERATIONAL_ID, Currency.getInstance("USD")));
  }

  public Map<String, Account> getAccountsById() {
    return this.accounts
      .getAll()
      .collect(Collectors.toMap(Account::getId, (account) -> account));
  }

  public Map<String, List<TransactionEntry>> getTransactionEntriesByAccountId() {
    return this.transactions
      .getAll()
      .map((transaction) -> Stream.of(transaction.getDebitEntry(), transaction.getCreditEntry()))
      .flatMap((doubleEntry) -> doubleEntry)
      .collect(Collectors.toList())
      .stream()
      .collect(Collectors.groupingBy((transactionEntry) -> transactionEntry.getAccount().getId()));
  }

  public Account putAccount(Account account) {
    return this.accounts.append(account);
  }

  public Transaction putTransaction(Transaction transaction) {
    return this.transactions.append(transaction);
  }
}