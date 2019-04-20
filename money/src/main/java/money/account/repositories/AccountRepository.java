package money.account.repositories;

import java.util.Optional;

import money.account.models.Account;

public interface AccountRepository {
  public Optional<Account> findById(String id);
}