package money.inmemory;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import money.account.models.Account;
import money.account.repositories.AccountRepository;

public class InMemoryAccountRepository implements AccountRepository {
  private final InMemoryStore store;

  public InMemoryAccountRepository(InMemoryStore store) {
    this.store = store;
  }

  @Override
  public CompletableFuture<Optional<Account>> findById(String id) {
    return CompletableFuture.completedFuture(
      Optional.ofNullable(store.getAccountsById().get(id))
    );
  }

  @Override
  public CompletableFuture<Account> create(Account account) {
    return CompletableFuture.completedFuture(
      store.putAccount(account)
    );
  }
}