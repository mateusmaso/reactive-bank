package money.transaction.repositories;

import money.MonetaryAmount;
import money.account.models.Account;
import money.transaction.models.Transaction;

public interface TransactionRepository {
  public MonetaryAmount sumBalance(Account account);
  public Transaction addTransaction(Transaction transaction);
}