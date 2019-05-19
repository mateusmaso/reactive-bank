package money.account;

import java.util.function.Supplier;

import money.account.Account;

public interface AccountOperation {
  public <V> V operateOn(Account account, Supplier<V> operation);
}