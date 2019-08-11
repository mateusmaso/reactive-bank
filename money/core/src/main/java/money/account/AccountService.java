package money.account;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.micrometer.core.instrument.Metrics;
import money.MonetaryAmount;
import money.account.AccountOperation;
import money.account.Account;
import money.account.AccountRepository;
import money.exceptions.AccountNotFoundException;
import money.exceptions.InsufficientFundsException;
import money.exceptions.InvalidTransactionException;
import money.transaction.Transaction;
import money.transaction.TransactionEntry;
import money.transaction.Transaction.TransactionType;
import money.transaction.TransactionRepository;

public class AccountService {
  private AccountOperation accountOperation;
  private AccountRepository accountRepository;
  private TransactionRepository transactionRepository;
  private Logger log;
  

  public AccountService(AccountOperation accountOperation, AccountRepository accountRepository, TransactionRepository transactionRepository) {
    this.accountOperation = accountOperation;
    this.accountRepository = accountRepository;
    this.transactionRepository = transactionRepository;
    this.log = LoggerFactory.getLogger(AccountService.class);
  }
  
  public CompletableFuture<Account> createAccount(Currency currency) {
    String id = UUID.randomUUID().toString();
    return this.accountRepository.create(new Account(id, currency));
  }

  public CompletableFuture<Transaction> transfer(String fromAccountId, String toAccountId, MonetaryAmount amount) {
    return getAccount(fromAccountId).thenCombine(getAccount(toAccountId), (debitAccount, creditAccount) -> {
      return addTransaction(TransactionType.TRANSFER, debitAccount, creditAccount, amount).join();
    });
  }

  public CompletableFuture<Transaction> load(String accountId, MonetaryAmount amount) {
    return getAccount(Account.OPERATIONAL_ID).thenCombine(getAccount(accountId), (debitAccount, creditAccount) -> {
      return addTransaction(TransactionType.LOAD, debitAccount, creditAccount, amount).join();
    });
  }

  public CompletableFuture<Transaction> unload(String accountId, MonetaryAmount amount) {
    return getAccount(accountId).thenCombine(getAccount(Account.OPERATIONAL_ID), (debitAccount, creditAccount) -> {
      return addTransaction(TransactionType.UNLOAD, debitAccount, creditAccount, amount).join();
    });
  }

  public CompletableFuture<MonetaryAmount> getBalance(String accountId) {
    return getAccount(accountId).thenCompose((account) -> transactionRepository.sumBalance(account));
  }

  private CompletableFuture<Transaction> addTransaction(TransactionType type, Account debitAccount, Account creditAccount, MonetaryAmount amount) {
    if (amount.getAmount().compareTo(BigDecimal.ZERO) < 1) {
      throw new InvalidTransactionException("Amount can't be zero or negative");
    }
    
    return accountOperation.operateOn(debitAccount, () -> {
      return getBalance(debitAccount.getId()).thenApply((balance) -> {
        if (!debitAccount.isOperational() && balance.getAmount().compareTo(amount.getAmount()) == -1) {
          throw new InsufficientFundsException();
        }

        Transaction transaction = new Transaction(
          UUID.randomUUID().toString(), 
          type, 
          TransactionEntry.debit(debitAccount, amount), 
          TransactionEntry.credit(creditAccount, amount)
        );

        Transaction createdTransaction = transactionRepository.create(transaction).join();
        log.info("Transaction of type {} created", createdTransaction.getType());
        Metrics.counter("transaction.created", "type", createdTransaction.getType().toString()).increment();

        return createdTransaction;
      });
    });
  }

  private CompletableFuture<Account> getAccount(String accountId) {
    return accountRepository.findById(accountId).thenApply((accountOpt) -> {
      return accountOpt.orElseThrow(() -> new AccountNotFoundException());
    });
  }
}