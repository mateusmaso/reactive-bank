package money;

import money.account.models.Account;

public class MonetaryMessage {
  private final Account account; 
  private final MonetaryAmount amount;

  public MonetaryMessage(Account account, MonetaryAmount amount) {
    this.account = account;
    this.amount = amount;
  }

  public Account getAccount() {
    return this.account;
  }

  public MonetaryAmount getAmount() {
    return this.amount;
  }
}