package money.store;

import java.util.Collections;
import java.util.Currency;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import money.account.models.Account;

public class AccountStore {
  private Map<String, Account> accountsById;

  public AccountStore() {
    this.accountsById = Collections.unmodifiableMap(new HashMap<>());
    this.put(new Account(Account.OPERATIONAL_ID, Currency.getInstance("USD")));
  }

  public Optional<Account> get(String accountId) {
    return Optional.ofNullable(accountsById.get(accountId));
  }

  public Account put(Account account) {
    Map<String, Account> modifiedMap = new HashMap<>();
    modifiedMap.putAll(this.accountsById);
    modifiedMap.put(account.getId(), account);
    this.accountsById = Collections.unmodifiableMap(modifiedMap);
    return account;
  }
}