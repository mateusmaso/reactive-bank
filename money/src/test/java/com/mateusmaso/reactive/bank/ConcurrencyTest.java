package com.mateusmaso.reactive.bank;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.function.Supplier;

import org.junit.Before;
import org.junit.Test;

import money.MonetaryAmount;
import money.account.Account;
import money.account.AccountOperation;
import money.account.AccountService;
import money.transaction.Transaction;
import money.inmemory.*;

public class ConcurrencyTest {
  private AccountService accountService;
  private InMemoryStore store;
  private InMemoryAccountOperation accountOperation;
  private InMemoryAccountRepository accountRepository;
  private InMemoryTransactionRepositoryWithBreakpoint transactionRepository;

  class InMemoryTransactionRepositoryWithBreakpoint extends InMemoryTransactionRepository {
    private CountDownLatch latch;

    public InMemoryTransactionRepositoryWithBreakpoint(InMemoryStore store) {
      super(store);
    }

    public void setLatch(CountDownLatch latch) {
      this.latch = latch;
    }

    @Override
    public CompletableFuture<Transaction> create(Transaction transaction) {
      if (this.latch != null) {
        try {
          this.latch.await();
        } catch(InterruptedException e) { throw new RuntimeException("Error"); }  
      }

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
    this.transactionRepository = new InMemoryTransactionRepositoryWithBreakpoint(store);
    this.accountService = new AccountService(this.accountOperation, this.accountRepository, this.transactionRepository);
  }

  @Test
  public void itShouldInsertOneTransferTransactionsWhenThereIsConcurrencyControl() throws InterruptedException {
    Account fromAccount = this.accountService.createAccount(Currency.getInstance("USD")).join();
    Account toAccount = this.accountService.createAccount(Currency.getInstance("USD")).join();

    this.accountService.load(fromAccount.getId(), MonetaryAmount.usd(BigDecimal.TEN)).join();

    CountDownLatch latch = new CountDownLatch(1);
    this.transactionRepository.setLatch(latch);
    
    Thread t1 = new Thread(new Runnable() {
      public void run() {
        accountService.transfer(fromAccount.getId(), toAccount.getId(), MonetaryAmount.usd(BigDecimal.TEN)).join();
      }
    });

    Thread t2 = new Thread(new Runnable() {
      public void run() {
        accountService.transfer(fromAccount.getId(), toAccount.getId(), MonetaryAmount.usd(BigDecimal.TEN)).join();
      }
    });

    t1.start();
    t2.start();
    latch.countDown();
    t1.join();
    t2.join();

    assertEquals(2, this.store.getTransactionEntriesByAccountId().get(fromAccount.getId()).size());
  }

  @Test
  public void itShouldWronglyInsertTwoTransferTransactionsWhenThereIsNotConcurrencyControl() throws InterruptedException {
    this.accountService = new AccountService(new DummyUnsafeOperation(), this.accountRepository, this.transactionRepository);

    Account fromAccount = this.accountService.createAccount(Currency.getInstance("USD")).join();
    Account toAccount = this.accountService.createAccount(Currency.getInstance("USD")).join();

    this.accountService.load(fromAccount.getId(), MonetaryAmount.usd(BigDecimal.TEN)).join();

    CountDownLatch latch = new CountDownLatch(1);
    this.transactionRepository.setLatch(latch);
    
    Thread t1 = new Thread(new Runnable() {
      public void run() {
        accountService.transfer(fromAccount.getId(), toAccount.getId(), MonetaryAmount.usd(BigDecimal.TEN)).join();
      }
    });

    Thread t2 = new Thread(new Runnable() {
      public void run() {
        accountService.transfer(fromAccount.getId(), toAccount.getId(), MonetaryAmount.usd(BigDecimal.TEN)).join();
      }
    });

    t1.start();
    t2.start();
    latch.countDown();
    t1.join();
    t2.join();

    assertEquals(3, this.store.getTransactionEntriesByAccountId().get(fromAccount.getId()).size());
  }
}