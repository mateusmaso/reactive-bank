package money.store;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import money.account.models.Account;
import money.transaction.models.TransactionEntry;

public class TransactionEntriesByAccountStore {
  private Map<String, List<TransactionEntry>> transactionEntriesByAccountId;

  public TransactionEntriesByAccountStore() {
    this.transactionEntriesByAccountId = Collections.unmodifiableMap(new HashMap<>());
  }

  public Optional<List<TransactionEntry>> get(Account account) {
    return Optional.ofNullable(transactionEntriesByAccountId.get(account.getId()));
  }

  public List<TransactionEntry> put(Account account, List<TransactionEntry> transactionEntries) {
    Map<String, List<TransactionEntry>> modifiedMap = new HashMap<>();
    modifiedMap.putAll(this.transactionEntriesByAccountId);
    modifiedMap.put(account.getId(), transactionEntries);
    this.transactionEntriesByAccountId = Collections.unmodifiableMap(modifiedMap);
    return transactionEntries;
  }
}