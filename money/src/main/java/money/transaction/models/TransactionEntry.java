package money.transaction.models;

import java.math.BigDecimal;
import java.util.Optional;

import money.MonetaryAmount;
import money.account.models.Account;
import money.exceptions.CurrencyNotAllowedException;

public class TransactionEntry {
  private Transaction transaction;
  private final Account account;
  private final MonetaryAmount amount;
  private final TransactionEntryType type;

  public enum TransactionEntryType {
    DEBIT,
    CREDIT
  }

  private TransactionEntry(Account account, MonetaryAmount amount, TransactionEntryType type) {
   if (!Optional.ofNullable(account).isPresent()) {
      throw new RuntimeException("Account must be present.");
    }

    if (!Optional.ofNullable(account).isPresent()) {
      throw new RuntimeException("Account must be present.");
    }

    if (!Optional.ofNullable(type).isPresent()) {
      throw new RuntimeException("Type must be present.");
    }
    
    if (!account.getCurrency().equals(amount.getCurrency())) {
      throw new CurrencyNotAllowedException();
    }

    this.account = account;
    this.amount = amount;
    this.type = type;
  }

  public static TransactionEntry debit(Account account, MonetaryAmount amount) {
    return new TransactionEntry(account, amount, TransactionEntryType.DEBIT);
  }

  public static TransactionEntry credit(Account account, MonetaryAmount amount) {
    return new TransactionEntry(account, amount, TransactionEntryType.CREDIT);
  }

  public boolean isDebit() {
    return this.type.equals(TransactionEntryType.DEBIT);
  }

  public boolean isCredit() {
    return this.type.equals(TransactionEntryType.CREDIT);
  }

  public Account getAccount() {
    return this.account;
  }

  public MonetaryAmount getAmount() {
    return this.amount;
  }

  public Transaction getTransaction() {
    return this.transaction;
  }

  public void setTransaction(Transaction transaction) {
    this.transaction = transaction;
  }

  public BigDecimal getSignedAmount() {
    if (this.isCredit()) {
      return this.getAmount().getAmount();
    } else {
      return this.getAmount().getAmount().negate();
    }
  }
}