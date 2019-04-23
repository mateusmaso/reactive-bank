package money;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import org.junit.Before;
import org.junit.Test;

import money.account.AccountOperation;
import money.account.Account;
import money.account.AccountRepository;
import money.account.AccountService;
import money.exceptions.InsufficientBalanceException;
import money.exceptions.InvalidTransactionException;
import money.transaction.Transaction;
import money.transaction.TransactionRepository;

public class AccountServiceTest {
  private AccountOperation accountOperationMock;
  private AccountRepository accountRepositoryMock;
  private TransactionRepository transactionRepositoryMock;
  private AccountService accountService;

  class DummyUnsafeOperation implements AccountOperation {
    @Override
    public <V> V operateOn(Account account, Supplier<V> operation) {
      return operation.get();
    }
  }

  @Before
  public void setUp() {
    this.accountOperationMock = new DummyUnsafeOperation();
    this.accountRepositoryMock = mock(AccountRepository.class);
    this.transactionRepositoryMock = mock(TransactionRepository.class);
    this.accountService = new AccountService(
      accountOperationMock,
      accountRepositoryMock,
      transactionRepositoryMock
    );
  }

  @Test
  public void itShouldGetBalance() {
    Account accountMock = createUsdAccountMock("abc123", BigDecimal.TEN);
    MonetaryAmount balance = accountService.getBalance(accountMock.getId()).join();

    assertEquals(balance.getAmount(), BigDecimal.TEN.setScale(2));
    assertEquals(balance.getCurrency(), Currency.getInstance("USD"));
  }

  @Test
  public void itShouldLoadAccount() {
    Account accountMock = createUsdAccountMock("abc123", BigDecimal.ZERO);
    createUsdAccountMock(Account.OPERATIONAL_ID, BigDecimal.ZERO);
    stubCreateTransaction();

    accountService.load(
      accountMock.getId(), 
      MonetaryAmount.usd(BigDecimal.TEN)
    ).join();

    verify(transactionRepositoryMock).create(argThat((transaction) -> {
      return transaction.getCreditEntry().getAccount().equals(accountMock) &&
            transaction.getCreditEntry().getSignedAmount().equals(BigDecimal.TEN.setScale(2));
    }));
  }

  @Test
  public void itShouldUnloadAccountIfHasBalance() {
    Account accountMock = createUsdAccountMock("abc123", BigDecimal.TEN);
    createUsdAccountMock(Account.OPERATIONAL_ID, BigDecimal.ZERO);
    stubCreateTransaction();

    accountService.unload(
      accountMock.getId(), 
      MonetaryAmount.usd(BigDecimal.TEN)
    ).join();

    verify(transactionRepositoryMock).create(argThat((transaction) -> {
      return transaction.getDebitEntry().getAccount().equals(accountMock) &&
            transaction.getDebitEntry().getSignedAmount().equals(BigDecimal.TEN.negate().setScale(2));
    }));
  }

  @Test
  public void itShouldNotUnloadAccountIfDoesNotHaveBalance() {
    Account accountMock = createUsdAccountMock("abc123", BigDecimal.ZERO);
    createUsdAccountMock(Account.OPERATIONAL_ID, BigDecimal.ZERO);
    stubCreateTransaction();

    accountService.unload(
      accountMock.getId(), 
      MonetaryAmount.usd(BigDecimal.TEN)
    ).whenComplete(
      (result, exception) -> {
        assertNotNull(exception);
        assertEquals(exception.getClass(), InsufficientBalanceException.class);
      }
    );
  }

  @Test
  public void itShouldNotTransferNegativeAmount() {
    Account accountMock1 = createUsdAccountMock("abc123", BigDecimal.TEN);
    Account accountMock2 = createUsdAccountMock("abc321", BigDecimal.ZERO);
    stubCreateTransaction();

    accountService.transfer(
      accountMock1.getId(), 
      accountMock2.getId(), 
      MonetaryAmount.usd(BigDecimal.TEN.negate())
    ).whenComplete(
      (result, exception) -> {
        assertNotNull(exception);
        assertEquals(exception.getClass(), InvalidTransactionException.class);
        assertEquals(exception.getMessage(), "Amount can't be negative");
      }
    );
  }

  @Test
  public void itShouldTransferBetweenAccountsIfHasBalance() {
    Account accountMock1 = createUsdAccountMock("abc123", BigDecimal.TEN);
    Account accountMock2 = createUsdAccountMock("abc321", BigDecimal.ZERO);
    stubCreateTransaction();

    accountService.transfer(
      accountMock1.getId(), 
      accountMock2.getId(), 
      MonetaryAmount.usd(BigDecimal.TEN)
    );

    verify(transactionRepositoryMock).create(argThat((transaction) -> {
      return transaction.getDebitEntry().getAccount().equals(accountMock1) &&
            transaction.getDebitEntry().getSignedAmount().equals(BigDecimal.TEN.negate().setScale(2)) &&
            transaction.getCreditEntry().getAccount().equals(accountMock2) &&
            transaction.getCreditEntry().getSignedAmount().equals(BigDecimal.TEN.setScale(2));
    }));
  }

  @Test
  public void itShouldNotTransferBetweenAccountsIfDoesNotHaveBalance() {
    Account accountMock1 = createUsdAccountMock("abc123", BigDecimal.ONE);
    Account accountMock2 = createUsdAccountMock("abc321", BigDecimal.ZERO);
    stubCreateTransaction();

    accountService.transfer(
      accountMock1.getId(), 
      accountMock2.getId(), 
      MonetaryAmount.usd(BigDecimal.TEN)
    ).whenComplete(
      (result, exception) -> {
        assertNotNull(exception);
        assertEquals(exception.getClass(), InsufficientBalanceException.class);
      }
    );
  }

  private Account createUsdAccountMock(String id, BigDecimal balance) {
    Account accountMock = new Account(id, Currency.getInstance("USD"));

    when(accountRepositoryMock.findById(id)).thenReturn(
      CompletableFuture.completedFuture(Optional.ofNullable(accountMock))
    );

    when(transactionRepositoryMock.sumBalance(accountMock)).thenReturn(
      CompletableFuture.completedFuture(MonetaryAmount.usd(BigDecimal.TEN))
    );

    return accountMock;
  }

  private void stubCreateTransaction() {
    when(transactionRepositoryMock.create(any())).thenReturn(
      CompletableFuture.completedFuture(mock(Transaction.class))
    );
  }
}