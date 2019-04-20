package money.account.services;

import money.MonetaryAmount;
import money.transaction.models.Transaction;

public interface AccountService {
  public Transaction transfer(String fromAccountId, String toAccountId, MonetaryAmount amount);
  public Transaction load(String accountId, MonetaryAmount amount);
  public Transaction unload(String accountId, MonetaryAmount amount);
  public MonetaryAmount getBalance(String accountId);
}