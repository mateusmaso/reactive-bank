package com.mateusmaso.reactive.bank;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.junit.Before;
import org.junit.Test;

import money.MonetaryAmount;
import money.account.Account;
import money.account.AccountOperation;
import money.account.AccountService;
import money.exceptions.InsufficientFundsException;
import money.transaction.Transaction;
import money.transaction.TransactionRepository;
import money.inmemory.*;

public class ConcurrencyTest {
  private AccountService accountService;
  private InMemoryStore store;
  private InMemoryAccountOperation accountOperation;
  private InMemoryAccountRepository accountRepository;
  private VerySlowInMemoryTransactionRepository transactionRepository;

  class VerySlowInMemoryTransactionRepository extends InMemoryTransactionRepository {
    private Integer waitTime;

    public VerySlowInMemoryTransactionRepository(InMemoryStore store, Integer waitTime) {
      super(store);
      this.waitTime = 0;
    }

    public void setWaitTime(Integer waitTime) {
      this.waitTime = waitTime;
    }

    @Override
    public CompletableFuture<Transaction> create(Transaction transaction) {
      try {
        TimeUnit.SECONDS.sleep(this.waitTime);
      } catch(InterruptedException e) { }
      
      return super.create(transaction);
    }
  }

  class DummyUnsafeOperation implements AccountOperation {
    @Override
    public <V> V operateOn(Account account, Supplier<V> operation) {
      return operation.get();
    }
  }

  @Before
  public void setUp() {
    this.store = new InMemoryStore();
    this.accountOperation = new InMemoryAccountOperation();
    this.accountRepository = new InMemoryAccountRepository(store);
    this.transactionRepository = new VerySlowInMemoryTransactionRepository(store, 0);
    this.accountService = new AccountService(this.accountOperation, this.accountRepository, this.transactionRepository);
  }

  @Test
  public void itShouldNotInsertTwoTransferTransactionsWhenThereIsConcurrencyControl() throws InterruptedException {
    Account fromAccount = this.accountService.createAccount(Currency.getInstance("USD")).join();
    Account toAccount = this.accountService.createAccount(Currency.getInstance("USD")).join();

    this.accountService.load(fromAccount.getId(), MonetaryAmount.usd(BigDecimal.TEN)).join();

    Thread t1 = new Thread(new Runnable() {
      public void run() {
        transactionRepository.setWaitTime(5);
        accountService
          .transfer(fromAccount.getId(), toAccount.getId(), MonetaryAmount.usd(BigDecimal.TEN))
          .whenComplete(
            (result, exception) -> {
              assertNull(exception);
              assertNotNull(result);
            }
          );
      }
    });

    t1.start();

    Thread t2 = new Thread(new Runnable() {
      public void run() {
        transactionRepository.setWaitTime(0);
        accountService
          .transfer(fromAccount.getId(), toAccount.getId(), MonetaryAmount.usd(BigDecimal.TEN))
          .whenComplete(
            (result, exception) -> {
              assertNotNull(exception);
              assertEquals(InsufficientFundsException.class, exception.getClass());
            }
          );
      }
    });

    t2.start();
    TimeUnit.SECONDS.sleep(2);

    assertEquals(
      2, 
      this.store.getTransactionEntriesByAccountId().get(fromAccount.getId()).size()
    );
  }

  @Test
  public void itShouldWronglyInsertTwoTransferTransactionsWhenThereIsNotConcurrencyControl() throws InterruptedException {
    this.accountService = new AccountService(new DummyUnsafeOperation(), this.accountRepository, this.transactionRepository);

    Account fromAccount = this.accountService.createAccount(Currency.getInstance("USD")).join();
    Account toAccount = this.accountService.createAccount(Currency.getInstance("USD")).join();

    this.accountService.load(fromAccount.getId(), MonetaryAmount.usd(BigDecimal.TEN)).join();

    Thread t1 = new Thread(new Runnable() {
      public void run() {
        transactionRepository.setWaitTime(5);
        accountService
          .transfer(fromAccount.getId(), toAccount.getId(), MonetaryAmount.usd(BigDecimal.TEN))
          .whenComplete(
            (result, exception) -> {
              assertNull(exception);
              assertNotNull(result);
            }
          );
      }
    });

    t1.start();

    Thread t2 = new Thread(new Runnable() {
      public void run() {
        transactionRepository.setWaitTime(0);
        accountService
          .transfer(fromAccount.getId(), toAccount.getId(), MonetaryAmount.usd(BigDecimal.TEN))
          .whenComplete(
            (result, exception) -> {
              assertNull(exception);
              assertNotNull(result);
            }
          );
      }
    });

    t2.start();
    TimeUnit.SECONDS.sleep(2);

    assertEquals(
      3, 
      this.store.getTransactionEntriesByAccountId().get(fromAccount.getId()).size()
    );
  }
}