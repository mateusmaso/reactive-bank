package money.inmemory;

import java.util.function.Supplier;

import money.account.AccountOperation;
import money.account.Account;

public class InMemoryAccountOperation implements AccountOperation {
  @Override
  public <V> V operateOn(Account account, Supplier<V> operation) {
    synchronized (account.getId()) {
      return operation.get();
    }
  }
}