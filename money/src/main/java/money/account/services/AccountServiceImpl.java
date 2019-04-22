package money.account.services;

import java.util.Currency;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import money.MonetaryAmount;
import money.account.AccountOperation;
import money.account.models.Account;
import money.account.repositories.AccountRepository;
import money.exceptions.AccountNotFoundException;
import money.exceptions.InsufficientBalanceException;
import money.transaction.models.Transaction;
import money.transaction.models.TransactionEntry;
import money.transaction.models.Transaction.TransactionType;
import money.transaction.repositories.TransactionRepository;

public class AccountServiceImpl implements AccountService {
  private AccountOperation accountOperation;
  private AccountRepository accountRepository;
  private TransactionRepository transactionRepository;

  public AccountServiceImpl(AccountOperation accountOperation, AccountRepository accountRepository, TransactionRepository transactionRepository) {
    this.accountOperation = accountOperation;
    this.accountRepository = accountRepository;
    this.transactionRepository = transactionRepository;
  }
  
  @Override
  public CompletableFuture<Account> createAccount(Currency currency) {
    String id = UUID.randomUUID().toString();
    return this.accountRepository.create(new Account(id, currency));
  }

  @Override
  public CompletableFuture<Transaction> transfer(String fromAccountId, String toAccountId, MonetaryAmount amount) {
    return getAccount(fromAccountId).thenCombine(getAccount(toAccountId), (debitAccount, creditAccount) -> {
      return addTransaction(TransactionType.TRANSFER, debitAccount, creditAccount, amount).join();
    });
  }

  @Override
  public CompletableFuture<Transaction> load(String accountId, MonetaryAmount amount) {
    return getAccount(Account.OPERATIONAL_ID).thenCombine(getAccount(accountId), (debitAccount, creditAccount) -> {
      return addTransaction(TransactionType.LOAD, debitAccount, creditAccount, amount).join();
    });
  }

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
    return accountOperation.operateOn(debitAccount, () -> {
      return getBalance(debitAccount.getId()).thenApply((balance) -> {
        if (!debitAccount.isOperational() && balance.getAmount().compareTo(amount.getAmount()) == -1) {
          throw new InsufficientBalanceException();
        }

        Transaction transaction = new Transaction(
          UUID.randomUUID().toString(), 
          type, 
          TransactionEntry.debit(debitAccount, amount), 
          TransactionEntry.credit(creditAccount, amount)
        );

        return transactionRepository.create(transaction).join();
      });
    });
  }

  private CompletableFuture<Account> getAccount(String accountId) {
    return accountRepository.findById(accountId).thenApply((accountOpt) -> {
      return accountOpt.orElseThrow(() -> new AccountNotFoundException());
    });
  }
}