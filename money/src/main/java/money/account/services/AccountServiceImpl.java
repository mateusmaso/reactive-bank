package money.account.services;

import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import money.MonetaryAmount;
import money.MonetaryEvent;
import money.account.models.Account;
import money.account.repositories.AccountRepository;
import money.exceptions.AccountNotFoundException;
import money.transaction.events.TransactionCreatedEvent;
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
  public CompletableFuture<Transaction> transfer(String fromAccountId, String toAccountId, MonetaryAmount amount) {
    return getAccount(fromAccountId).thenCombine(getAccount(toAccountId), (debitAccount, creditAccount) -> {
      return addTransaction(TransactionType.TRANSFER, debitAccount, creditAccount, amount).join();
    });
  }

  // ATOMIC
  @Override
  public CompletableFuture<Transaction> load(String accountId, MonetaryAmount amount) {
    return getAccount(Account.OPERATIONAL_ID).thenCombine(getAccount(accountId), (debitAccount, creditAccount) -> {
      return addTransaction(TransactionType.LOAD, debitAccount, creditAccount, amount).join();
    });
  }

  // ATOMIC
  @Override
  public CompletableFuture<Transaction> unload(String accountId, MonetaryAmount amount) {
    return getAccount(accountId).thenCombine(getAccount(Account.OPERATIONAL_ID), (debitAccount, creditAccount) -> {
      return addTransaction(TransactionType.UNLOAD, debitAccount, creditAccount, amount).join();
    });
  }

  @Override
  public CompletableFuture<MonetaryAmount> getBalance(String accountId) {
    return getAccount(accountId).thenCompose((account) -> transactionRepository.sumBalance(account));
  }

  private CompletableFuture<Transaction> addTransaction(TransactionType type, Account debitAccount, Account creditAccount, MonetaryAmount amount) {
    // if (getBalance(debitAccount.getId())) {

    // }

    SortedSet<TransactionEntry> doubleEntry = new TreeSet<>();
    doubleEntry.add(TransactionEntry.debit(debitAccount, amount));
    doubleEntry.add(TransactionEntry.credit(creditAccount, amount));

    String id = UUID.randomUUID().toString();
    Transaction transaction = new Transaction(id, type, doubleEntry);

    return transactionRepository.addTransaction(transaction).thenApply((persistedTransaction) -> {
      MonetaryEvent event = new TransactionCreatedEvent(persistedTransaction);
      return persistedTransaction;
    });
  }

  private CompletableFuture<Account> getAccount(String accountId) {
    return accountRepository.findById(accountId).thenApply((accountOpt) -> {
      return accountOpt.orElseThrow(() -> new AccountNotFoundException());
    });
  }
}