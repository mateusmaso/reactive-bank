package money.account;

import money.MonetaryMessage;
import money.account.models.Account;

public interface AccountMessageInbox {
  public void send(Account account, MonetaryMessage message);
  public MonetaryMessage read();
}