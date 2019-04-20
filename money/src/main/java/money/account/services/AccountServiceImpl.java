package money.account.services;

import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

import money.MonetaryAmount;
import money.account.models.Account;
import money.account.repositories.AccountRepository;
import money.exceptions.AccountNotFoundException;
import money.transaction.models.Transaction;
import money.transaction.models.TransactionEntry;
import money.transaction.models.Transaction.TransactionType;
import money.transaction.repositories.TransactionRepository;

public class AccountServiceImpl implements AccountService {
  private AccountRepository accountRepository;
  private TransactionRepository transactionRepository;

  public AccountServiceImpl(AccountRepository accountRepository, TransactionRepository transactionRepository) {
    this.accountRepository = accountRepository;
    this.transactionRepository = transactionRepository;
  }

  // ATOMIC
  @Override
  public Transaction transfer(String fromAccountId, String toAccountId, MonetaryAmount amount) {
    Account debitAccount = getAccount(fromAccountId);
    Account creditAccount = getAccount(toAccountId);
    return addTransaction(TransactionType.TRANSFER, debitAccount, creditAccount, amount);
  }

  // ATOMIC
  @Override
  public Transaction load(String accountId, MonetaryAmount amount) {
    Account debitAccount = getAccount(Account.OPERATIONAL_ID);
    Account creditAccount = getAccount(accountId);
    return addTransaction(TransactionType.LOAD, debitAccount, creditAccount, amount);
  }

  // ATOMIC
  @Override
  public Transaction unload(String accountId, MonetaryAmount amount) {
    Account debitAccount = getAccount(accountId);
    Account creditAccount = getAccount(Account.OPERATIONAL_ID);
    return addTransaction(TransactionType.UNLOAD, debitAccount, creditAccount, amount);
  }

  @Override
  public MonetaryAmount getBalance(String accountId) {
    return transactionRepository.sumBalance(getAccount(accountId));
  }

  private Transaction addTransaction(TransactionType type, Account debitAccount, Account creditAccount, MonetaryAmount amount) {
    // check balance here => 

    SortedSet<TransactionEntry> doubleEntry = new TreeSet<>();
    doubleEntry.add(TransactionEntry.debit(debitAccount, amount));
    doubleEntry.add(TransactionEntry.credit(creditAccount, amount));

    String id = UUID.randomUUID().toString();
    return transactionRepository.addTransaction(new Transaction(id, type, doubleEntry));
  }

  private Account getAccount(String accountId) {
    return accountRepository.findById(accountId).orElseThrow(() -> new AccountNotFoundException());
  }
}