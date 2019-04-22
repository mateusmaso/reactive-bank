package money.inmemory;

import org.junit.Before;
import org.junit.Test;

import money.MonetaryAmount;
import money.account.models.Account;
import money.transaction.models.TransactionEntry;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryTransactionRepositoryTest {
  private InMemoryStore storeMock;
  private InMemoryTransactionRepository repository;

  @Before
  public void setUp() {
    this.storeMock = mock(InMemoryStore.class);
    this.repository = new InMemoryTransactionRepository(this.storeMock);
  }

  @Test
  public void itShouldCalculateRightSumBalance() {
    Account accountMock = mock(Account.class);
    when(accountMock.getId()).thenReturn("abc123");
    when(accountMock.getCurrency()).thenReturn(Currency.getInstance("USD"));

    List<TransactionEntry> transactionEntries = new ArrayList<>();
    transactionEntries.add(TransactionEntry.credit(accountMock, new MonetaryAmount(Currency.getInstance("USD"), BigDecimal.TEN)));
    transactionEntries.add(TransactionEntry.credit(accountMock, new MonetaryAmount(Currency.getInstance("USD"), BigDecimal.TEN)));
    transactionEntries.add(TransactionEntry.debit(accountMock, new MonetaryAmount(Currency.getInstance("USD"), BigDecimal.TEN)));
    
    Map<String, List<TransactionEntry>> transactionEntriesByAccountId = new HashMap<>();
    transactionEntriesByAccountId.put(accountMock.getId(), transactionEntries);
    when(storeMock.getTransactionEntriesByAccountId()).thenReturn(transactionEntriesByAccountId);

    MonetaryAmount balance = this.repository.sumBalance(accountMock).join();
    assertEquals(balance.getAmount(), BigDecimal.TEN.setScale(2));
  }

  @Test
  public void itShouldSumZeroWhenAccountDoesNotHaveTransactionsYet() {
    Account accountMock = mock(Account.class);
    when(accountMock.getId()).thenReturn("abc123");
    when(accountMock.getCurrency()).thenReturn(Currency.getInstance("USD"));

    Map<String, List<TransactionEntry>> transactionEntriesByAccountId = new HashMap<>();
    when(storeMock.getTransactionEntriesByAccountId()).thenReturn(transactionEntriesByAccountId);

    MonetaryAmount balance = this.repository.sumBalance(accountMock).join();
    assertEquals(balance.getAmount(), BigDecimal.ZERO.setScale(2));
  }
}