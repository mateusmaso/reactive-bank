package money.account.repositories;

import java.util.Optional;

import money.account.models.Account;
import money.store.AccountStore;

public class AccountRepositoryImpl implements AccountRepository {
  private final AccountStore accountStore;

  public AccountRepositoryImpl(AccountStore accountStore) {
    this.accountStore = accountStore;
  }

  @Override
  public Optional<Account> findById(String id) {
    return accountStore.get(id);
  }
}