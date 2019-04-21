package money.inmemory;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import money.account.models.Account;
import money.account.repositories.AccountRepository;

public class InMemoryAccountRepository implements AccountRepository {
  private final AccountStore accountStore;

  public InMemoryAccountRepository(AccountStore accountStore) {
    this.accountStore = accountStore;
  }

  @Override
  public CompletableFuture<Optional<Account>> findById(String id) {
    return CompletableFuture.supplyAsync(() -> {
      return accountStore.get(id);
    });
  }
}