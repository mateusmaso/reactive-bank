package money.account.repositories;

import java.util.Optional;

import money.account.models.Account;
import money.store.AccountStore;

public class AccountRepositoryInMemory implements AccountRepository {
  private final AccountStore accountStore;

  public AccountRepositoryInMemory(AccountStore accountStore) {
    this.accountStore = accountStore;
  }

  @Override
  public Optional<Account> findById(String id) {
    return accountStore.get(id);
  }
}