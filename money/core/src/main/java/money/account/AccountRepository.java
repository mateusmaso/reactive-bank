package money.account;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import money.account.Account;

public interface AccountRepository {
  public CompletableFuture<Optional<Account>> findById(String id);
  public CompletableFuture<Account> create(Account account);
}