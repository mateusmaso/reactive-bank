package money.inmemory;

import org.junit.Before;
import org.junit.Test;

import money.MonetaryAmount;
import money.account.Account;
import money.transaction.Transaction;
import money.transaction.TransactionEntry;
import money.transaction.Transaction.TransactionType;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;

public class InMemoryStoreTest {
  private InMemoryStore store;

  @Before
  public void setUp() {
    this.store = new InMemoryStore();
  }

  @Test
  public void itShouldAlwaysReturnAnAccountsByIdMapReference() {
    assertNotNull(this.store.getAccountsById());
  }

  @Test
  public void itShouldNotGetAccountByIdWhenNotInserted() {
    assertNull(this.store.getAccountsById().get("abc123"));
  }

  @Test
  public void itShouldGetAccountByIdWhenInserted() {
    Account account = new Account("abc123", Currency.getInstance("USD"));
    this.store.putAccount(account);
    assertEquals(this.store.getAccountsById().get("abc123"), account);
  }

  @Test
  public void itShouldAlwaysReturnATransactionEntriesByAccountIdMapReference() {
    assertNotNull(this.store.getTransactionEntriesByAccountId());
  }

  @Test
  public void itShouldNotGetTransactionEntriesByAccountIdWhenNotInserted() {
    assertNull(this.store.getTransactionEntriesByAccountId().get("abc123"));
  }

  @Test
  public void itShouldGetTransactionEntriesByAccountIdWhenInserted() {
    Account debitAccount = new Account("123", Currency.getInstance("USD"));
    Account creditAccount = new Account("321", Currency.getInstance("USD"));
    MonetaryAmount amount = MonetaryAmount.usd(BigDecimal.TEN);

    Transaction transaction = new Transaction(
      "abc123", 
      TransactionType.TRANSFER, 
      TransactionEntry.debit(debitAccount, amount), 
      TransactionEntry.credit(creditAccount, amount)
    );

    this.store.putTransaction(transaction);

    List<TransactionEntry> debitAccountTransactionEntries = this.store
      .getTransactionEntriesByAccountId()
      .get(debitAccount.getId());

    List<TransactionEntry> creditAccountTransactionEntries = this.store
      .getTransactionEntriesByAccountId()
      .get(creditAccount.getId());

    assertNotNull(debitAccountTransactionEntries);
    assertNotNull(creditAccountTransactionEntries);

    assertEquals(debitAccountTransactionEntries.size(), 1);
    assertEquals(
      debitAccountTransactionEntries.get(0).getSignedAmount(),
      BigDecimal.TEN.negate().setScale(2)
    );

    assertEquals(creditAccountTransactionEntries.size(), 1);
    assertEquals(
      creditAccountTransactionEntries.get(0).getSignedAmount(),
      BigDecimal.TEN.setScale(2)
    );
  }
}